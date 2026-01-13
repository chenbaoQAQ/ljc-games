Page({
  data: {
    logs: [],
    generals: [],
    genIndex: 0,
    selectedGeneralId: null,
    maxLeadership: 0,
    currentSpace: 0, // 初始必须定义
    troopAssignment: [], // 初始必须定义
    stageId: 1,
    showBattle: false
  },

  onShow() { // 改用 onShow 确保每次进入页面都刷新数据
    this.fetchGenerals();
  },

  fetchGenerals() {
    wx.request({
      url: 'http://localhost:8888/api/game/generals',
      data: { userId: 1 },
      success: (res) => {
        if (res.data && res.data.length > 0) {
          const gen = res.data[this.data.genIndex] || res.data[0];
          this.setData({ 
            generals: res.data,
            selectedGeneralId: gen.id,
            maxLeadership: gen.maxLeadership || 100
          });
          this.initTroopList(gen);
        }
      }
    });
  },

  initTroopList(general) {
    // 定义常量：名称映射和空间占用
    const labels = { 'INFANTRY': '步兵', 'ARCHER': '弓兵', 'CAVALRY': '骑兵', 'CN_SPECIAL': '羽林' };
    const spaces = { 'INFANTRY': 1, 'ARCHER': 1, 'CAVALRY': 2, 'CN_SPECIAL': 2 };
    
    // 安全解析 JSON：如果字段为空则使用默认空对象
    let reserve = {};
    try {
      reserve = JSON.parse(general.reserveArmyConfigStr || "{}");
    } catch (e) {
      console.error("仓库数据解析失败", e);
    }

    // 将对象转为列表
    const list = Object.keys(reserve).map(key => ({
      name: key,
      label: labels[key] || key,
      reserve: reserve[key],
      space: spaces[key] || 1,
      assigned: 0 // 初始分配为0
    }));

    this.setData({ troopAssignment: list, currentSpace: 0 });
  },

  onCountInput(e) {
    const index = e.currentTarget.dataset.index;
    const val = parseInt(e.detail.value) || 0;
    let list = this.data.troopAssignment;
    
    // 校验：不能超过库存
    list[index].assigned = Math.min(val, list[index].reserve);
    
    // 计算总占用空间
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

    wx.showLoading({ title: '全军出击...' });
    wx.request({
      url: 'http://localhost:8888/api/game/assign-troops', // 先调分配接口
      method: 'POST',
      header: { 'content-type': 'application/x-www-form-urlencoded' },
      data: {
        generalId: this.data.selectedGeneralId,
        config: JSON.stringify(finalConfig)
      },
      success: (assignRes) => {
        // 分配成功后再开始战斗
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
      complete: () => wx.hideLoading()
    });
  },

  resetBattle() {
    this.setData({ showBattle: false, logs: [] });
    this.fetchGenerals();
  }
});