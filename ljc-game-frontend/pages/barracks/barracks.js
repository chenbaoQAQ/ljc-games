Page({
  data: {
    userId: 1,
    generals: [], // 拥有的武将列表
    index: 0,
    selectedGeneralId: null,
    gold: 0,
    unitList: [
      { name: 'INFANTRY', label: '步兵', cost: 10 },
      { name: 'ARCHER', label: '弓兵', cost: 10 },
      { name: 'CAVALRY', label: '骑兵', cost: 10 }
    ]
  },

  onShow() {
    this.fetchGold();
    this.fetchGenerals();
  },

  // 获取金币
  fetchGold() {
    wx.request({
      url: 'http://localhost:8888/api/game/profile',
      data: { userId: this.data.userId },
      success: (res) => { this.setData({ gold: res.data.gold }); }
    });
  },

  // 获取该玩家的所有武将
  fetchGenerals() {
    wx.request({
      url: 'http://localhost:8888/api/game/generals',
      data: { userId: this.data.userId },
      success: (res) => {
        if (res.data && res.data.length > 0) {
          this.setData({ 
            generals: res.data,
            selectedGeneralId: res.data[0].id // 默认选第一个
          });
        }
      }
    });
  },

  // 切换武将选择
  bindGeneralChange(e) {
    const idx = e.detail.value;
    this.setData({
      index: idx,
      selectedGeneralId: this.data.generals[idx].id
    });
  },

  // 执行招募
  doRecruit(e) {
    if (!this.data.selectedGeneralId) {
      wx.showToast({ title: '请先选择武将', icon: 'none' });
      return;
    }
    const unitName = e.currentTarget.dataset.name;
    const count = 50; 
    
    wx.showLoading({ title: '招募中...' });
    wx.request({
      url: 'http://localhost:8888/api/game/recruit',
      method: 'POST',
      header: { 'content-type': 'application/x-www-form-urlencoded' },
      data: {
        userId: this.data.userId,
        generalId: this.data.selectedGeneralId, // 使用选中的武将ID
        unitName: unitName,
        count: count
      },
      success: (res) => {
        wx.showModal({ title: '结果', content: res.data, showCancel: false });
        this.fetchGold(); 
        this.fetchGenerals(); // 招募完刷新武将带兵状态
      },
      complete: () => { wx.hideLoading(); }
    });
  },

  goBack() { wx.navigateBack(); }
});