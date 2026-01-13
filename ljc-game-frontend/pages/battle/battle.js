Page({
  data: {
    logs: [],
    generals: [],
    genIndex: 0,
    selectedGeneralId: null,
    maxLeadership: 0, // 对应数据库 max_leadership
    currentSpace: 0, 
    troopAssignment: [], 
    stageId: 1,
    showBattle: false
  },

  onShow() {
    this.fetchGenerals();
  },

  // 1. 获取武将列表及统帅上限
  fetchGenerals() {
    wx.request({
      url: 'http://localhost:8888/api/game/generals',
      data: { userId: 1 },
      success: (res) => {
        if (res.data && res.data.length > 0) {
          const gen = res.data[this.data.genIndex];
          this.setData({ 
            generals: res.data,
            selectedGeneralId: gen.id,
            // 💡 核心：读取后端 maxLeadership 字段
            maxLeadership: gen.maxLeadership || 100 
          });
          this.initTroopList(gen);
        }
      }
    });
  },

  // 2. 初始化可分配兵力列表（从仓库提取）
  initTroopList(general) {
    const labels = { 
      'INFANTRY': '常规步兵', 
      'ARCHER': '常规弓兵', 
      'CAVALRY': '常规骑兵', 
      'CN_SPECIAL': '汉之羽林' 
    };
    // 💡 对应 SQL 中的 space_cost
    const spaces = { 
      'INFANTRY': 1, 
      'ARCHER': 1, 
      'CAVALRY': 2, 
      'CN_SPECIAL': 2 
    };
    
    let reserve = {};
    try {
      // 💡 读取仓库字段 reserveArmyConfigStr
      reserve = JSON.parse(general.reserveArmyConfigStr || "{}");
    } catch (e) {
      console.error("仓库数据解析失败", e);
    }

    const list = Object.keys(reserve).map(key => ({
      name: key,
      label: labels[key] || key,
      reserve: reserve[key],
      space: spaces[key] || 1,
      assigned: 0 
    }));

    this.setData({ troopAssignment: list, currentSpace: 0 });
  },

  // 3. 实时分配兵力并计算统帅值占用
  onCountInput(e) {
    const index = e.currentTarget.dataset.index;
    const val = parseInt(e.detail.value) || 0;
    let list = this.data.troopAssignment;
    
    // 校验：分配数不能超过仓库库存
    list[index].assigned = Math.min(val, list[index].reserve);
    
    // 计算总占用空间（数量 * 占用系数）
    let totalSpace = 0;
    list.forEach(item => {
      totalSpace += (item.assigned * item.space);
    });

    this.setData({ troopAssignment: list, currentSpace: totalSpace });
  },

  // 4. 开始战斗：先分配上阵，后触发战斗
  startBattle() {
    if (this.data.currentSpace > this.data.maxLeadership) {
      wx.showModal({ title: '统帅警告', content: '分配兵力超过上限！', showCancel: false });
      return;
    }
    if (this.data.currentSpace <= 0) {
      wx.showToast({ title: '请至少分配1名士兵', icon: 'none' });
      return;
    }

    // 组装分配 JSON
    const finalConfig = {};
    this.data.troopAssignment.forEach(item => {
      if (item.assigned > 0) finalConfig[item.name] = item.assigned;
    });

    wx.showLoading({ title: '全军出击...' });
    
    // A. 提交分配方案
    wx.request({
      url: 'http://localhost:8888/api/game/assign-troops',
      method: 'POST',
      data: finalConfig, // 后端 @RequestBody 接收 Map
      header: { 'content-type': 'application/json' },
      success: (assignRes) => {
        if (assignRes.data === "SUCCESS") {
          // B. 分配成功，触发战斗引擎
          wx.request({
            url: 'http://localhost:8888/api/battle/start',
            data: {
              userId: 1,
              generalId: this.data.selectedGeneralId,
              stageId: this.data.stageId
            },
            success: (battleRes) => {
              this.setData({ logs: battleRes.data, showBattle: true });
            }
          });
        } else {
          wx.showModal({ title: '分配失败', content: assignRes.data, showCancel: false });
        }
      },
      complete: () => wx.hideLoading()
    });
  },

  // 5. 战斗结束重置状态
  resetBattle() {
    this.setData({ 
      showBattle: false, 
      logs: [],
      genIndex: 0 
    });
    this.fetchGenerals();
  },

  bindGenChange(e) {
    this.setData({ genIndex: e.detail.value }, () => {
      this.fetchGenerals();
    });
  }
});