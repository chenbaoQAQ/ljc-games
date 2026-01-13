Page({
  data: {
    logs: [],
    generals: [],
    genIndex: 0,
    selectedGeneralId: null,
    maxLeadership: 0,
    currentSpace: 0,
    troopAssignment: [],
    stageId: 1,
    showBattle: false
  },

  onShow() {
    this.fetchGenerals();
  },

  fetchGenerals() {
    wx.request({
      url: 'http://localhost:8888/api/game/generals',
      data: { userId: 1 },
      success: (res) => {
        // 增加数据存在性校验
        if (res.data && res.data.length > 0) {
          const gen = res.data[this.data.genIndex] || res.data[0];
          this.setData({ 
            generals: res.data,
            selectedGeneralId: gen.id,
            maxLeadership: gen.maxLeadership || 120 
          });
          this.initTroopList(gen);
        } else {
          console.warn("未获取到武将数据");
          this.setData({ troopAssignment: [] });
        }
      },
      fail: () => {
        wx.showToast({ title: '网络连接失败', icon: 'none' });
      }
    });
  },

  initTroopList(general) {
    if (!general) return;
    
    const labels = { 'INFANTRY': '步兵', 'ARCHER': '弓兵', 'CAVALRY': '骑兵', 'CN_SPECIAL': '羽林' };
    const spaces = { 'INFANTRY': 1, 'ARCHER': 1, 'CAVALRY': 2, 'CN_SPECIAL': 2 };
    
    let reserve = {};
    // 💡 极其关键的防崩溃处理
    try {
      if (general.reserveArmyConfigStr && general.reserveArmyConfigStr !== "") {
        reserve = JSON.parse(general.reserveArmyConfigStr);
      }
    } catch (e) {
      console.error("仓库数据JSON解析崩溃，请检查后端返回:", e);
      reserve = {}; 
    }

    const list = Object.keys(reserve).map(key => ({
      name: key,
      label: labels[key] || key,
      reserve: reserve[key],
      space: spaces[key] || 1,
      assigned: 0
    })).filter(item => item.reserve > 0); // 只显示有库存的兵种

    this.setData({ troopAssignment: list, currentSpace: 0 });
  },

  onCountInput(e) {
    const index = e.currentTarget.dataset.index;
    const val = parseInt(e.detail.value) || 0;
    let list = this.data.troopAssignment;
    
    if (!list[index]) return;

    list[index].assigned = Math.min(val, list[index].reserve);
    
    let totalSpace = 0;
    list.forEach(item => {
      totalSpace += (item.assigned * item.space);
    });

    this.setData({ troopAssignment: list, currentSpace: totalSpace });
  },

  startBattle() {
    if (this.data.currentSpace > this.data.maxLeadership) {
      wx.showModal({ title: '统帅警告', content: '分配兵力超过上限！', showCancel: false });
      return;
    }
    if (this.data.currentSpace <= 0) {
      wx.showToast({ title: '请至少分配1名士兵', icon: 'none' });
      return;
    }

    const finalConfig = {};
    this.data.troopAssignment.forEach(item => {
      if (item.assigned > 0) finalConfig[item.name] = item.assigned;
    });

    wx.showLoading({ title: '准备战斗...' });

    wx.request({
      url: 'http://localhost:8888/api/game/assign-troops',
      method: 'POST',
      header: { 'content-type': 'application/x-www-form-urlencoded' },
      data: {
        generalId: this.data.selectedGeneralId,
        config: JSON.stringify(finalConfig)
      },
      success: (res) => {
        // 兼容后端返回 SUCCESS 字符串或成功提示
        if (res.data && (res.data === "SUCCESS" || res.data.indexOf("成功") !== -1)) {
          this.triggerBattle();
        } else {
          wx.showModal({ title: '分配失败', content: res.data || "后端未响应", showCancel: false });
        }
      },
      complete: () => wx.hideLoading()
    });
  },

  triggerBattle() {
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
  },

  resetBattle() {
    this.setData({ showBattle: false, logs: [] });
    this.fetchGenerals();
  },

  bindGenChange(e) {
    this.setData({ genIndex: e.detail.value }, () => {
      this.fetchGenerals();
    });
  }
});