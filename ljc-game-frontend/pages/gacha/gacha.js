Page({
  data: {
    result: '准备招募武将...',
    userId: 1
  },
  doGacha() {
    wx.showLoading({ title: '正在招募...' });
    wx.request({
      url: 'http://localhost:8888/api/game/gacha',
      method: 'POST',
      header: { 'content-type': 'application/x-www-form-urlencoded' },
      data: { userId: this.data.userId },
      success: (res) => {
        // 后端返回的是纯字符串
        this.setData({ result: res.data });
      },
      fail: () => {
        wx.showToast({ title: '招募请求失败', icon: 'none' });
      },
      complete: () => { wx.hideLoading(); }
    });
  }
})