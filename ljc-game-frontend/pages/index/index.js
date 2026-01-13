Page({
  /**
   * 页面的初始数据
   */
  data: {
    gold: 0,
    diamond: 0,
    userId: 1 // 当前测试使用的玩家ID
  },

  /**
   * 生命周期函数--监听页面显示
   * 每次从其他页面返回主页时，都会触发这个函数，自动刷新金币和钻石
   */
  onShow: function () {
    this.fetchProfile();
  },

  /**
   * 从后端获取玩家存档信息
   */
  fetchProfile: function () {
    const that = this;
    wx.request({
      // 如果是在手机上预览，请把 localhost 换成你电脑的局域网 IP
      url: 'http://localhost:8888/api/game/profile',
      method: 'GET',
      data: {
        userId: this.data.userId
      },
      success: (res) => {
        // res.data 对应后端的 UserProfile 实体对象
        if (res.statusCode === 200 && res.data) {
          that.setData({
            gold: res.data.gold,
            diamond: res.data.diamond
          });
        }
      },
      fail: (err) => {
        console.error("获取存档失败：", err);
        wx.showToast({
          title: '服务器连接失败',
          icon: 'none'
        });
      }
    });
  },

  /**
   * 跳转到点将台（抽卡页面）
   */
  goToGacha: function () {
    wx.navigateTo({
      url: '/pages/gacha/gacha'
    });
  },
  /**
   * 跳转到点兵台（前往兵营）
   */
  goToBarracks() {
    wx.navigateTo({ url: '/pages/barracks/barracks' });
  },
  /**
   * 跳转到战斗页面（开始闯关）
   */
  startBattle: function () {
    // 跳转到我们新创建的战斗日志页面
    wx.navigateTo({
      url: '/pages/battle/battle'
    });
  },

  /**
   * 下拉刷新功能
   */
  onPullDownRefresh: function () {
    this.fetchProfile();
    wx.stopPullDownRefresh();
  }
})