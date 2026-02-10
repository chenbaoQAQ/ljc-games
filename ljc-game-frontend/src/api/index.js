// API 基础配置
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

// HTTP 请求封装
class API {
    // 通用 JSON 请求
    static async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers,
            },
            ...options,
        };

        try {
            const response = await fetch(url, config);
            const contentType = response.headers.get('content-type') || '';

            let data;
            if (contentType.includes('application/json')) {
                data = await response.json();
            } else {
                // 后端返回纯字符串的情况
                data = await response.text();
            }

            if (!response.ok) {
                const msg = (typeof data === 'object' && data.message) ? data.message : data;
                throw new Error(msg || `请求失败 (${response.status})`);
            }

            return data;
        } catch (error) {
            console.error(`API Error [${endpoint}]:`, error);
            throw error;
        }
    }

    // GET 请求：参数拼到 URL
    static get(endpoint, params) {
        const query = params ? '?' + new URLSearchParams(params).toString() : '';
        return this.request(endpoint + query, { method: 'GET' });
    }

    // POST 请求：JSON Body
    static post(endpoint, body) {
        return this.request(endpoint, {
            method: 'POST',
            body: body ? JSON.stringify(body) : undefined,
        });
    }

    // POST 请求：URL 查询参数 + JSON Body (后端很多接口是 @RequestParam + @RequestBody 混合)
    static postWithParams(endpoint, params, body) {
        const query = params ? '?' + new URLSearchParams(params).toString() : '';
        return this.request(endpoint + query, {
            method: 'POST',
            body: body ? JSON.stringify(body) : undefined,
        });
    }
}

// ============ 认证 API ============
export const authAPI = {
    // 注册：@RequestBody RegisterReq {username, password, nickname, initialCiv}
    register(username, password, nickname, initialCiv) {
        return API.post('/auth/register', { username, password, nickname, initialCiv });
    },

    // 登录：@RequestBody LoginReq {username, password}  → 返回纯字符串
    login(username, password) {
        return API.post('/auth/login', { username, password });
    },
};

// ============ 玩家 API ============
export const playerAPI = {
    // GET /player/info?userId=xxx → Result<PlayerInfoResp>
    getInfo(userId) {
        return API.get('/player/info', { userId });
    },

    // GET /player/progress?userId=xxx → Result<List<UserCivProgressTbl>>
    getProgress(userId) {
        return API.get('/player/progress', { userId });
    },
};

// ============ 大厅 API ============
export const hallAPI = {
    // GET /hall/generals?userId=xxx → Result<List<UserGeneralTbl>>
    getGenerals(userId) {
        return API.get('/hall/generals', { userId });
    },

    // GET /hall/progress?userId=xxx → Result<List<UserCivProgressTbl>>
    getProgress(userId) {
        return API.get('/hall/progress', { userId });
    },

    // POST /hall/general/upgrade?userId=xxx&generalId=xxx → Result<String>
    upgradeGeneral(userId, generalId) {
        return API.postWithParams('/hall/general/upgrade', { userId, generalId });
    },

    // POST /hall/general/activate?userId=xxx&generalId=xxx → Result<String>
    activateGeneral(userId, generalId) {
        return API.postWithParams('/hall/general/activate', { userId, generalId });
    },

    // POST /hall/general/ascend?userId=xxx&generalId=xxx → Result<String>
    ascendGeneral(userId, generalId) {
        return API.postWithParams('/hall/general/ascend', { userId, generalId });
    },

    // POST /hall/recruit?userId=xxx  Body: {troopId, count} → Result<String>
    recruit(userId, troopId, count) {
        return API.postWithParams('/hall/recruit', { userId }, { troopId, count });
    },

    // POST /hall/general/equip?userId=xxx  Body: {generalId, equipmentId} → Result<String>
    equipGeneral(userId, generalId, equipmentId) {
        return API.postWithParams('/hall/general/equip', { userId }, { generalId, equipmentId });
    },

    // POST /hall/equipment/enhance?userId=xxx&equipmentId=xxx → Result<String>
    enhanceEquipment(userId, equipmentId) {
        return API.postWithParams('/hall/equipment/enhance', { userId, equipmentId });
    },

    // POST /hall/skill/learn?userId=xxx&generalId=xxx&bookItemId=xxx → Result<String>
    learnSkill(userId, generalId, bookItemId) {
        return API.postWithParams('/hall/skill/learn', { userId, generalId, bookItemId });
    },

    // POST /hall/gem/inlay?userId=xxx  Body: {equipmentId, socketIndex, gemId} → Result<String>
    inlayGem(userId, equipmentId, socketIndex, gemId) {
        return API.postWithParams('/hall/gem/inlay', { userId }, { equipmentId, socketIndex, gemId });
    },

    // POST /hall/gem/combine?userId=xxx  Body: {gemType, level} → Result<String>
    combineGem(userId, gemType, level) {
        return API.postWithParams('/hall/gem/combine', { userId }, { gemType, level });
    },

    // GET /hall/equipments?userId=xxx
    getEquipments(userId) {
        return API.get('/hall/equipments', { userId });
    },

    // GET /hall/gems?userId=xxx
    getGems(userId) {
        return API.get('/hall/gems', { userId });
    },

    // GET /hall/items?userId=xxx
    getItems(userId) {
        return API.get('/hall/items', { userId });
    },
};

// ============ 战斗 API ============
export const battleAPI = {
    // POST /battle/story/start?userId=xxx  Body: {civ, stageNo, generalId, troopConfig}
    startStoryBattle(userId, civ, stageNo, generalId, troopConfig) {
        return API.postWithParams('/battle/story/start', { userId }, { civ, stageNo, generalId, troopConfig });
    },

    // POST /battle/turn?userId=xxx  Body: {castSkill}
    processTurn(userId, castSkill, clientTurnNo) {
        return API.postWithParams('/battle/turn', { userId }, { castSkill, clientTurnNo });
    },

    // GET /battle/state?userId=xxx
    getBattleState(userId) {
        return API.get('/battle/state', { userId });
    },
};

// ============ 关卡 API ============
export const stageAPI = {
    getStages(civ) {
        return API.get('/stage/list', { civ });
    },
};

// ============ 兵力 API ============
export const troopAPI = {
    getTroops(userId) {
        return API.get('/troop/list', { userId });
    },
};

export default API;
