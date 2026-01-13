Page({
  data: {
    logs: [],
    generals: [],
    genIndex: 0,
    selectedGeneralId: null,
    stageId: 1, // 默认第一关
    showBattle: false // 是否开始显示战斗日志
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
          this.setData({ 
            generals: res.data,
            selectedGeneralId: res.data[0].id 
          });
        }
      }
    });
  },

  bindGenChange(e) {
    this.setData({
      genIndex: e.detail.value,
      selectedGeneralId: this.data.generals[e.detail.value].id
    });
  },

  startBattle() {
    if (!this.data.selectedGeneralId) return;
    wx.showLoading({ title: '全军出击...' });
    wx.request({
      url: 'http://localhost:8888/api/battle/start',
      data: {
        userId: 1,
        generalId: this.data.selectedGeneralId, // 传选中的武将
        stageId: this.data.stageId
      },
      success: (res) => {
        this.setData({ logs: res.data, showBattle: true });
      },
      complete: () => wx.hideLoading()
    });
  }
});