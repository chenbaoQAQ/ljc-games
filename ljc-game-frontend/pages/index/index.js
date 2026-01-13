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
   * 每次从其他页面返回主页时，都会触发刷新
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
        if (res.statusCode === 200 && res.data) {
          // --- 核心拦截逻辑 ---
          // 检查玩家是否已经选择了国家
          if (!res.data.unlockedCountries || res.data.unlockedCountries === "") {
            console.log("检测到玩家未选择国家，准备跳转...");
            wx.reLaunch({
              url: '/pages/select-country/select' 
            });
            return; // 拦截成功后直接返回，不再执行后续赋值
          }

          // 如果已经选过国家，则正常更新数据
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
  goToBarracks: function () {
    wx.navigateTo({
      url: '/pages/barracks/barracks'
    });
  },

  /**
   * 跳转到战斗页面（开始闯关）
   */
  startBattle: function () {
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