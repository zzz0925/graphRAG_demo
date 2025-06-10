/* ================================ */
/* chat.js */
/* ================================ */
class ChatApp {
    constructor() {
        this.currentSessionId = null;
        this.isLoading = false;
        this.neo4jViz = null; // 添加 Neo4jViz 实例
        this.initializeElements();
        this.attachEventListeners();
        this.loadChatHistory();
        this.initNeo4jViz(); // 初始化 Neo4j 可视化
        this.fileInput = document.createElement('input');
        this.fileInput.type = 'file';
        this.fileInput.accept = '.txt,.pdf';
        this.fileInput.style.display = 'none';
        document.body.appendChild(this.fileInput);
        this.attachFileUploadEventListeners();
    }

    initializeElements() {
        this.messageInput = document.getElementById('message-input');
        this.sendBtn = document.getElementById('send-btn');
        this.chatMessages = document.getElementById('chat-messages');
        this.loadingIndicator = document.getElementById('loading-indicator');
        this.newChatBtn = document.getElementById('new-chat-btn');
        this.charCount = document.querySelector('.char-count');
        this.refreshGraphBtn = document.getElementById('refresh-graph-btn'); // 新增刷新按钮
        this.sessionSelector = document.getElementById('session-selector'); // 新增会话选择器
        this.uploadBtn = document.getElementById('upload-btn');
    }

    attachEventListeners() {
        this.sendBtn.addEventListener('click', () => {
            this.sendMessage();
        });

        this.messageInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });

        this.messageInput.addEventListener('input', () => {
            this.updateSendButton();
            this.updateCharCount();
            this.autoResize();
        });

        this.newChatBtn.addEventListener('click', () => {
            this.startNewChat();
        });

        this.messageInput.addEventListener('paste', (e) => {
            setTimeout(() => {
                this.updateSendButton();
                this.updateCharCount();
                this.autoResize();
            }, 0);
        });

        // Neo4j 刷新按钮事件
        this.refreshGraphBtn.addEventListener('click', () => {
            this.neo4jViz.loadGraphData(); // 刷新整个图
        });

        // 会话选择器事件
        this.sessionSelector.addEventListener('change', (event) => {
            const selectedSessionId = event.target.value;
            if (selectedSessionId) {
                this.neo4jViz.loadGraphData(selectedSessionId); // 加载特定会话的图
            } else {
                this.neo4jViz.loadGraphData(); // 加载所有图
            }
        });

        this.uploadBtn.addEventListener('click', () => {
           this.fileInput.click();
        });
    }
    //------------------------------------------新增方法----------
    attachFileUploadEventListeners() {
        this.fileInput.addEventListener('change', async (event) => {
            const file = event.target.files[0];
            if (file) {
                const fileName = file.name
                const formData = new FormData();
                formData.append('file', file);

                try {
                    this.showLoading();

                    const response = await fetch('http://localhost:8080/api/vector/doc2Vec', {
                        method: 'POST',
                        body: formData
                    });

                    if (!response.ok) {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }

                    const result = await response.json();
                    this.addMessageToUI('assistant', `File processed successfully: ${JSON.stringify(result)}`);

                } catch (error) {
                    console.error('文件上传错误:', error);
                    this.showError('文件上传失败，请重试');
                } finally {
                    this.hideLoading();
                }
            }
        });
    }
    //----------------------
    updateSendButton() {
            const hasContent = this.messageInput.value.trim().length > 0;
            this.sendBtn.disabled = !hasContent || this.isLoading;
    }

    updateCharCount() {
            const currentLength = this.messageInput.value.length;
            const maxLength = 2000;
            this.charCount.textContent = `${currentLength}/${maxLength}`;

            if (currentLength > maxLength * 0.9) {
                this.charCount.style.color = '#f56565';
            } else {
                this.charCount.style.color = '#666';
            }
    }

    autoResize() {
            this.messageInput.style.height = 'auto';
            const scrollHeight = this.messageInput.scrollHeight;
            const maxHeight = 120;
            this.messageInput.style.height = Math.min(scrollHeight, maxHeight) + 'px';
    }

    initNeo4jViz() {
        // 传递必要的元素ID给 Neo4jViz
        this.neo4jViz = new Neo4jViz('neo4j-graph', 'graph-loading', 'session-selector');
        this.neo4jViz.loadGraphData(); // 初始加载全部图数据
        this.populateSessionSelector(); // 填充会话选择器
    }

    // ... (sendMessage, callChatAPI, addMessageToUI, showLoading, hideLoading, showError, scrollToBottom, clearChatMessages, addWelcomeMessage, saveChatHistory, loadChatHistory, loadMessagesFromServer 保持不变) ...

    async sendMessage() {
        const message = this.messageInput.value.trim();
        if (!message || this.isLoading) {
            return;
        }

        try {
            this.isLoading = true;
            this.updateSendButton();

            this.addMessageToUI('user', message);

            this.messageInput.value = '';
            this.updateCharCount();
            this.autoResize();

            this.showLoading();

            const response = await this.callChatAPI(message);

            if (response.success) {
                if (response.sessionId) {
                    this.currentSessionId = response.sessionId;
                    this.saveChatHistory();
                }

                // 处理流式数据
                if (response.stream) {
                    const reader = response.stream.getReader();
                    const decoder = new TextDecoder('utf-8');
                    let done = false;
                    let accumulatedResponse = '';

                    while (!done) {
                        const { value, done: doneReading } = await reader.read();
                        done = doneReading;
                        const chunkValue = decoder.decode(value, { stream: !done });
                        accumulatedResponse += chunkValue;

                        // 假设服务器每行返回一个 JSON 对象
                        const lines = accumulatedResponse.split('\n');
                        lines.forEach((line, index) => {
                            if (index === lines.length - 1 && !done) {
                                // 最后一行可能不完整，暂不处理
                                accumulatedResponse = line;
                            } else if (line.trim().length > 0) {
                                try {
                                    const jsonResponse = JSON.parse(line);
                                    if (jsonResponse.response) {
                                        this.addMessageToUI('assistant', jsonResponse.response);
                                    }
                                } catch (parseError) {
                                    console.error('解析流式数据出错:', parseError);
                                }
                            }
                        });
                    }
                } else if (response.response) {
                    this.addMessageToUI('assistant', response.response);
                }

                // 消息发送并保存后，刷新 Neo4j 图
                this.neo4jViz.loadGraphData();
                this.populateSessionSelector(); // 刷新会话选择器
            } else {
                this.showError(response.error || '发送消息失败，请重试');
            }

        } catch (error) {
            console.error('发送消息错误:', error);
            this.showError('网络错误，请检查连接后重试');
        } finally {
            this.isLoading = false;
            this.hideLoading();
            this.updateSendButton();
            this.messageInput.focus();
        }
    }

    async callChatAPI(message) {
        const requestBody = {
            message: message,
            sessionId: this.currentSessionId
        };

        const response = await fetch('http://localhost:8080/api/chat/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody)
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        // 检查响应是否为流式数据
        if (response.body) {
            return { success: true, stream: response.body };
        }

        return await response.json();
    }


    async startNewChat() {
        if (confirm('确定要开始新对话吗？当前对话将被清除。')) {
            this.currentSessionId = null;
            this.clearChatMessages();
            this.addWelcomeMessage();
            this.clearChatHistory();
            this.messageInput.focus();
            this.neo4jViz.loadGraphData(); // 新对话也刷新图
            this.populateSessionSelector(); // 刷新会话选择器
        }
    }

    // 新增方法：获取并填充会话选择器
    async populateSessionSelector() {
        try {
            // 假设你有一个 API 来获取所有会话ID
            const response = await fetch('/api/chat/sessions'); // 你需要实现这个API
            if (response.ok) {
                const sessions = await response.json(); // 假设返回 { sessionId: '...', createdTime: '...' } 列表

                this.sessionSelector.innerHTML = '<option value="">选择会话查看图谱</option>'; // 清空并添加默认选项

                sessions.forEach(session => {
                    const option = document.createElement('option');
                    option.value = session.sessionId;
                    option.textContent = `会话: ${session.sessionId.substring(0, 8)}... (${new Date(session.createdTime).toLocaleDateString()})`;
                    this.sessionSelector.appendChild(option);
                });

                // 如果当前有会话ID，则选中它
                if (this.currentSessionId) {
                    this.sessionSelector.value = this.currentSessionId;
                }
            }
        } catch (error) {
            console.error('获取会话列表失败:', error);
        }
    }
    addMessageToUI(role, content) {
            const messageDiv = document.createElement('div');
            messageDiv.className = `message ${role}-message`;

            const avatarDiv = document.createElement('div');
            avatarDiv.className = 'message-avatar';

            const avatar = document.createElement('div');
            avatar.className = `avatar ${role}-avatar`;
            avatar.textContent = role === 'user' ? '您' : 'AI';
            avatarDiv.appendChild(avatar);

            const contentDiv = document.createElement('div');
            contentDiv.className = 'message-content';

            const textDiv = document.createElement('div');
            textDiv.className = 'message-text';
            textDiv.textContent = content;
            contentDiv.appendChild(textDiv);

            messageDiv.appendChild(avatarDiv);
            messageDiv.appendChild(contentDiv);

            // 判断 loadingIndicator 是否为 chatMessages 的子节点
            if (this.chatMessages.contains(this.loadingIndicator)) {
                this.chatMessages.insertBefore(messageDiv, this.loadingIndicator);
            } else {
                this.chatMessages.appendChild(messageDiv);
            }

            // 滚动到底部
            this.scrollToBottom();
        }

        showLoading() {
            this.loadingIndicator.style.display = 'flex';
            this.scrollToBottom();
        }

    hideLoading() {
        this.loadingIndicator.style.display = 'none';
    }
    showError(message) {
            const errorDiv = document.createElement('div');
            errorDiv.className = 'error-message';
            errorDiv.textContent = '错误: ' + message;

            this.chatMessages.insertBefore(errorDiv, this.loadingIndicator);
            this.scrollToBottom();

            // 5秒后自动移除错误消息
            setTimeout(() => {
                if (errorDiv.parentNode) {
                    errorDiv.parentNode.removeChild(errorDiv);
                }
            }, 5000);
    }

    scrollToBottom() {
        setTimeout(() => {
            this.chatMessages.scrollTop = this.chatMessages.scrollHeight;
        }, 100);
    }


    clearChatMessages() {
            // 保留加载指示器，清除其他消息
            const messages = this.chatMessages.querySelectorAll('.message:not(#loading-indicator)');
            messages.forEach(message => message.remove());

            // 清除错误消息
            const errorMessages = this.chatMessages.querySelectorAll('.error-message');
            errorMessages.forEach(error => error.remove());
    }

    addWelcomeMessage() {
        this.addMessageToUI('assistant', '您好！我是您的AI助手，有什么我可以帮助您的吗？');
    }

    // 本地存储相关方法
    saveChatHistory() {
        if (!this.currentSessionId) return;

        const chatData = {
            sessionId: this.currentSessionId,
            timestamp: new Date().toISOString()
        };

        localStorage.setItem('currentChat', JSON.stringify(chatData));
    }

    loadChatHistory() {
        try {
            const savedChat = localStorage.getItem('currentChat');
            if (savedChat) {
                const chatData = JSON.parse(savedChat);
                this.currentSessionId = chatData.sessionId;
                this.loadMessagesFromServer();
            }
        } catch (error) {
            console.error('加载聊天历史失败:', error);
        }
    }

    async loadMessagesFromServer() {
        if (!this.currentSessionId) return;

        try {
            const response = await fetch(`/api/chat/history/${this.currentSessionId}`);
            if (response.ok) {
                const messages = await response.json();
                this.clearChatMessages();

                messages.forEach(msg => {
                    this.addMessageToUI(msg.role, msg.content);
                });

                if (messages.length === 0) {
                    this.addWelcomeMessage();
                }
            }
        } catch (error) {
            console.error('从服务器加载消息失败:', error);
            this.addWelcomeMessage();
        }
    }

    clearChatHistory() {
        localStorage.removeItem('currentChat');
    }
}


// ============== Neo4j 可视化类 ==============
class Neo4jViz {
    constructor(containerId, loadingIndicatorId, sessionSelectorId) {
        this.container = document.getElementById(containerId);
        this.loadingIndicator = document.getElementById(loadingIndicatorId);
        this.sessionSelector = document.getElementById(sessionSelectorId);
        this.network = null; // vis.js Network 实例
        this.nodes = new vis.DataSet(); // vis.js 数据集
        this.edges = new vis.DataSet(); // vis.js 数据集
        this.options = {
            nodes: {
                shape: 'dot',
                size: 15,
                font: {
                    size: 12,
                    color: '#333'
                },
                borderWidth: 2
            },
            edges: {
                width: 1,
                arrows: 'to',
                font: {
                    size: 10,
                    align: 'middle'
                },
                color: {
                    color: '#848484',
                    highlight: '#848484',
                    hover: '#848484'
                },
                dashes: false
            },
            physics: {
                enabled: true,
                barnesHut: {
                    gravitationalConstant: -2000,
                    centralGravity: 0.3,
                    springLength: 95,
                    springConstant: 0.04,
                    damping: 0.09,
                    avoidOverlap: 0.5
                },
                maxVelocity: 50,
                minVelocity: 0.1,
                solver: 'barnesHut',
                stabilization: {
                    iterations: 2500
                }
            },
            interaction: {
                navigationButtons: true,
                keyboard: true
            },
            layout: {
                randomSeed: undefined,
                improvedLayout: true
            }
        };
    }

    async loadGraphData(sessionId = null) {
        this.showLoading();
        try {
            const url = sessionId ? `http://localhost:8080/api/neo4j/graph-data/${sessionId}` : 'http://localhost:8080/api/neo4j/graph-data';
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const graphData = await response.json();

            this.nodes.clear();
            this.edges.clear();

            if (graphData.nodes && graphData.relationships) { // 注意这里是 graphData.relationships
                // 转换节点数据
                const visNodes = graphData.nodes.map(node => {
                    let label = '';
                    let color = '#97C2E6'; // 默认节点颜色

                    // 根据节点类型设置 label 和颜色
                    if (node.labels && node.labels.includes('Conversation')) {
                        label = `会话: ${node.sessionId}`;
                        color = '#FFD700'; // 会话节点颜色
                    } else if (node.labels && node.labels.includes('Message')) {
                        label = node.content;
                        color = '#ADD8E6'; // 消息节点颜色
                    } else {
                        // 如果有其他类型的节点，可以继续添加逻辑
                        label = node.name || node.id; // 尝试用name属性，否则用id
                    }

                    return {
                        id: node.id,
                        label: label,
                        title: JSON.stringify(node.properties, null, 2), // 鼠标悬停时显示所有属性
                        color: color // 为不同类型的节点设置不同颜色
                    };
                });

                // 转换边（关系）数据
                const visEdges = graphData.relationships.map(edge => {
                    return {
                        id: edge.id,
                        from: edge.inV,
                        to: edge.outV,
                        label: edge.label, // 将关系类型作为边的标签
                        title: JSON.stringify(edge.pros, null, 2), // 鼠标悬停时显示属性
                        arrows: 'to'
                    };
                });

                this.nodes.add(visNodes);
                this.edges.add(visEdges);

                if (visNodes.length === 0 && visEdges.length === 0) {
                     console.warn('Neo4j graph data is empty after transformation.');
                     this.nodes.add({ id: 'empty', label: '图谱为空', color: 'red' });
                }

            } else {
                console.warn('Neo4j graph data is empty or malformed:', graphData);
                this.nodes.add({ id: 'empty', label: '图谱为空', color: 'red' });
            }

            this.renderGraph();
        } catch (error) {
            console.error('加载 Neo4j 图数据失败:', error);
            this.nodes.clear();
            this.edges.clear();
            this.nodes.add({ id: 'error', label: '加载图谱失败: ' + error.message, color: 'red' });
            this.renderGraph();
        } finally {
            this.hideLoading();
        }
    }

    renderGraph() {
        const data = {
            nodes: this.nodes,
            edges: this.edges
        };

        if (this.network) {
            this.network.destroy(); // 销毁旧网络实例
        }
        this.network = new vis.Network(this.container, data, this.options);

        // 可选：添加点击事件，例如点击节点显示详情
        this.network.on("click", (params) => {
            if (params.nodes.length > 0) {
                const nodeId = params.nodes[0];
                const node = this.nodes.get(nodeId);
                console.log("Clicked node:", node);
                // 可以在这里显示节点属性的弹窗或侧边栏
            }
            if (params.edges.length > 0) {
                const edgeId = params.edges[0];
                const edge = this.edges.get(edgeId);
                console.log("Clicked edge:", edge);
            }
        });
    }

    showLoading() {
        this.loadingIndicator.style.display = 'flex';
    }

    hideLoading() {
        this.loadingIndicator.style.display = 'none';
    }
}


// 页面加载完成后初始化应用
document.addEventListener('DOMContentLoaded', () => {
    window.chatApp = new ChatApp();
});

// 页面卸载前保存状态
window.addEventListener('beforeunload', () => {
    if (window.chatApp && window.chatApp.currentSessionId) {
        window.chatApp.saveChatHistory();
    }
});