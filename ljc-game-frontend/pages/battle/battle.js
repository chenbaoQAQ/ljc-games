Page({
  data: {
    logs: [],
    generals: [],
    genIndex: 0,
    selectedGeneralId: null,
    maxLeadership: 0,
    currentSpace: 0,
    troopAssignment: [], // 用于存放待分配的兵力信息
    stageId: 1,
    showBattle: false
  },

  onLoad() {
    this.fetchGenerals();
  },

  fetchGenerals() {
    wx.request({
      url: 'http://localhost:8888/api/game/generals',
      data: { userId: 1 },
      success: (res) => {
        if (res.data.length > 0) {
          const gen = res.data[0];
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
    // 假设你的后端现在返回两个 JSON：armyConfigStr(上阵) 和 reserveArmyConfigStr(库存)
    // 这里简单演示：我们假设所有兵力都在“待分配”状态
    const labels = { 'INFANTRY': '步兵', 'ARCHER': '弓兵', 'CAVALRY': '骑兵', 'CN_SPECIAL': '羽林' };
    const spaces = { 'INFANTRY': 1, 'ARCHER': 1, 'CAVALRY': 2, 'CN_SPECIAL': 2 };
    
    // 解析库存（此处逻辑需配合你后端的字段名）
    const reserve = JSON.parse(general.armyConfigStr || "{}"); 
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
    
    // 校验不能超过库存
    list[index].assigned = Math.min(val, list[index].reserve);
    
    // 重新计算总占用空间
    let totalSpace = 0;
    list.forEach(item => {
      totalSpace += (item.assigned * item.space);
    });

    this.setData({ troopAssignment: list, currentSpace: totalSpace });
  },

  startBattle() {
    if (this.data.currentSpace > this.data.maxLeadership) {
      wx.showToast({ title: '超过统帅上限！', icon: 'none' });
      return;
    }

    // 将分配好的数据转为后端需要的 JSON 格式
    const finalConfig = {};
    this.data.troopAssignment.forEach(item => {
      if (item.assigned > 0) finalConfig[item.name] = item.assigned;
    });

    wx.showLoading({ title: '全军出击...' });
    // 注意：这里需要你后端接口支持传分配好的 armyConfig 参数
    wx.request({
      url: 'http://localhost:8888/api/battle/start-with-config', 
      method: 'POST',
      header: { 'content-type': 'application/x-www-form-urlencoded' },
      data: {
        userId: 1,
        generalId: this.data.selectedGeneralId,
        stageId: this.data.stageId,
        armyConfig: JSON.stringify(finalConfig)
      },
      success: (res) => {
        this.setData({ logs: res.data, showBattle: true });
      },
      complete: () => wx.hideLoading()
    });
  },

  resetBattle() {
    this.setData({ showBattle: false });
    this.fetchGenerals();
  }
});