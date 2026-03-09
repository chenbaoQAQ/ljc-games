import { troopAPI } from '../api/index.js';
import { router } from '../utils/router.js';

export function TreeCodexPage(container) {
    const userId = localStorage.getItem('userId');
    if (!userId) {
        router.navigate('/login');
        return;
    }

    const state = {
        treeData: null,
        selectedCiv: 'ALL',
    };

    container.innerHTML = `
        <div class="tree-codex-page">
            <nav class="page-nav">
                <button class="btn-back" id="tree-back-btn">← 返回招募</button>
                <h1 class="page-title">兵种进化图鉴</h1>
            </nav>

            <div class="civ-selector">
                <button class="btn-civ active" data-civ="ALL">全兵种</button>
                <button class="btn-civ" data-civ="CN">魏蜀吴</button>
                <button class="btn-civ" data-civ="JP">战国</button>
                <button class="btn-civ" data-civ="KR">朝鲜</button>
                <button class="btn-civ" data-civ="GB">英伦</button>
            </div>

            <div class="tree-viewport" id="tree-viewport">
                <svg id="tree-links" class="tree-links"></svg>
                <div id="tree-nodes" class="tree-nodes"></div>
            </div>

            <div id="node-detail-panel" class="node-detail-panel hidden">
                <div class="detail-content">
                    <h3 id="detail-name"></h3>
                    <p id="detail-status"></p>
                    <p id="detail-desc"></p>
                    <div id="detail-actions"></div>
                    <button class="btn-close-detail" id="tree-close-detail">关闭</button>
                </div>
            </div>

            <div class="toast" id="tree-toast"></div>
        </div>

        <style>
            .tree-codex-page {
                min-height: 100vh;
                display: flex;
                flex-direction: column;
                background: #1b2140;
                color: #fff;
            }
            .page-nav {
                display: flex;
                align-items: center;
                gap: 12px;
                padding: 12px 16px;
                background: rgba(0, 0, 0, 0.3);
            }
            .btn-back {
                border: 0;
                padding: 8px 12px;
                border-radius: 8px;
                cursor: pointer;
                background: #43d2d1;
                color: #0a2240;
                font-weight: 700;
            }
            .page-title {
                margin: 0;
                font-size: 22px;
            }
            .civ-selector {
                padding: 8px 16px;
            }
            .btn-civ {
                border: 1px solid #4d5d86;
                background: #273158;
                color: #fff;
                padding: 6px 12px;
                border-radius: 8px;
            }
            .btn-civ.active {
                border-color: #ffe56a;
                color: #ffe56a;
            }
            .tree-viewport {
                position: relative;
                overflow: auto;
                flex: 1;
                background: #151937;
            }
            .tree-links {
                position: absolute;
                top: 0;
                left: 0;
                width: 3600px;
                height: 1200px;
                z-index: 1;
                pointer-events: none;
            }
            .tree-nodes {
                position: absolute;
                top: 0;
                left: 0;
                width: 3600px;
                height: 1200px;
                z-index: 2;
            }
            .tree-node {
                position: absolute;
                width: 92px;
                height: 92px;
                border-radius: 10px;
                border: 2px solid #6070a0;
                background: #2f385f;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                text-align: center;
                font-size: 12px;
                cursor: pointer;
                padding: 4px;
                box-sizing: border-box;
            }
            .tree-node.LOCKED { filter: grayscale(1); opacity: .7; }
            .tree-node.BRANCH_LOCKED { border-color: #ff5a5a; opacity: .55; }
            .tree-node.DISCOVERED { border-color: #ffcf4a; box-shadow: 0 0 10px rgba(255, 207, 74, .5); }
            .tree-node.UNLOCKED { border-color: #53e4b7; }
            .tree-node.EVOLVED { border-color: #53e4b7; box-shadow: 0 0 12px rgba(83, 228, 183, .5); }
            .node-detail-panel {
                position: absolute;
                bottom: 0;
                left: 0;
                right: 0;
                z-index: 10;
                background: rgba(6, 8, 20, .95);
                border-top: 1px solid #3e4b74;
                padding: 16px;
            }
            .node-detail-panel.hidden { display: none; }
            .btn-evolve {
                border: 0;
                border-radius: 8px;
                background: #ff8a48;
                color: #fff;
                font-weight: 700;
                padding: 8px 12px;
                cursor: pointer;
            }
            .btn-close-detail {
                margin-top: 10px;
                border: 0;
                border-radius: 8px;
                background: #2e3a66;
                color: #fff;
                padding: 6px 10px;
                cursor: pointer;
            }
            .toast {
                position: fixed;
                left: 50%;
                bottom: 24px;
                transform: translateX(-50%);
                background: rgba(0, 0, 0, 0.75);
                color: #fff;
                border-radius: 8px;
                padding: 8px 16px;
                opacity: 0;
                transition: opacity 0.2s;
            }
            .toast.show { opacity: 1; }
        </style>
    `;

    document.getElementById('tree-back-btn').addEventListener('click', () => router.navigate('/recruit'));
    document.getElementById('tree-close-detail').addEventListener('click', () => {
        document.getElementById('node-detail-panel').classList.add('hidden');
    });
    container.querySelectorAll('.btn-civ').forEach((btn) => {
        btn.addEventListener('click', async () => {
            const civ = btn.dataset.civ;
            if (!civ || civ === state.selectedCiv) return;
            state.selectedCiv = civ;
            container.querySelectorAll('.btn-civ').forEach((b) => b.classList.remove('active'));
            btn.classList.add('active');
            await loadTree();
        });
    });

    loadTree();

    async function loadTree() {
        try {
            const res = await troopAPI.getTree(userId, state.selectedCiv);
            if (res.code === 200) {
                state.treeData = res.data;
                renderTree();
            } else {
                showToast(res.message || '加载失败');
            }
        } catch (e) {
            console.error(e);
            showToast('加载失败');
        }
    }

    function renderTree() {
        const viewport = document.getElementById('tree-viewport');
        const linksSvg = document.getElementById('tree-links');
        const nodesContainer = document.getElementById('tree-nodes');
        linksSvg.innerHTML = '';
        nodesContainer.innerHTML = '';

        if (!state.treeData) return;

        const gridX = 150;
        const gridY = 140;
        const baseX = 1700;
        const baseY = 60;

        state.treeData.nodes.forEach((node) => {
            const el = document.createElement('div');
            el.className = `tree-node ${node.status}`;

            const posX = baseX + ((node.xPos || 0) * gridX);
            const posY = baseY + ((node.tier || 0) * gridY);
            node._renderX = posX;
            node._renderY = posY;

            el.style.left = `${posX}px`;
            el.style.top = `${posY}px`;
            el.innerHTML = `
                <div>${node.name}</div>
                ${node.status === 'LOCKED' ? '🔒' : ''}
                ${node.status === 'BRANCH_LOCKED' ? '❌' : ''}
            `;
            el.onclick = () => openDetail(node);
            nodesContainer.appendChild(el);
        });

        state.treeData.edges.forEach((edge) => {
            const from = state.treeData.nodes.find((n) => n.nodeId === edge.from);
            const to = state.treeData.nodes.find((n) => n.nodeId === edge.to);
            if (!from || !to) return;

            const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
            line.setAttribute('x1', from._renderX + 46);
            line.setAttribute('y1', from._renderY + 92);
            line.setAttribute('x2', to._renderX + 46);
            line.setAttribute('y2', to._renderY);
            line.setAttribute('stroke', '#7082b6');
            line.setAttribute('stroke-width', '2');
            linksSvg.appendChild(line);
        });

        // 渲染后把视口移动到树中心区域，避免初始只看到空白边缘
        if (viewport) {
            viewport.scrollLeft = 1200;
            viewport.scrollTop = 0;
        }
    }

    function openDetail(node) {
        const panel = document.getElementById('node-detail-panel');
        panel.classList.remove('hidden');

        document.getElementById('detail-name').textContent = `${node.name}${node.tier > 0 ? ` (Tier ${node.tier})` : ' (基础)'}`;
        document.getElementById('detail-status').textContent = `状态: ${getStatusText(node.status)}`;
        document.getElementById('detail-desc').textContent = node.unlockHint || '';

        const actions = document.getElementById('detail-actions');
        actions.innerHTML = '';
        if (node.isEvolvable) {
            const btn = document.createElement('button');
            btn.className = 'btn-evolve';
            btn.textContent = `进化 (消耗 ${node.evolveCost} 金币)`;
            btn.onclick = () => handleEvolve(node);
            actions.appendChild(btn);
        }
    }

    function getStatusText(status) {
        const map = {
            LOCKED: '未解锁',
            DISCOVERED: '可进化',
            UNLOCKED: '已解锁',
            EVOLVED: '已进化',
            BRANCH_LOCKED: '分支已锁定',
        };
        return map[status] || status;
    }

    async function handleEvolve(node) {
        if (!confirm(`确定要进化为 ${node.name} 吗？\n该操作会锁定同级互斥分支。`)) return;

        try {
            const res = await troopAPI.evolveNode(userId, {
                fromNodeId: node.parentNodeId,
                toNodeId: node.nodeId,
            });
            if (res.code === 200) {
                showToast('进化成功');
                document.getElementById('node-detail-panel').classList.add('hidden');
                await loadTree();
            } else {
                showToast(res.message || '进化失败');
            }
        } catch (e) {
            showToast(e.message || '请求失败');
        }
    }

    function showToast(message) {
        const toast = document.getElementById('tree-toast');
        toast.textContent = message;
        toast.classList.add('show');
        setTimeout(() => toast.classList.remove('show'), 1800);
    }
}
