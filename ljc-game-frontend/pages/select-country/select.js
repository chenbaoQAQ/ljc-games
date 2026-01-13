Page({
  onSelect(e) {
    const code = e.currentTarget.dataset.code;
    const names = { 'CN': '大汉', 'GB': '不列颠', 'JP': '东瀛', 'KR': '高丽' };
    
    wx.showModal({
      title: '效忠确认',
      content: `你决定作为 ${names[code]} 的领主开启征程吗？`,
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: 'http://localhost:8888/api/game/choose-country',
            method: 'POST',
            header: { 'content-type': 'application/x-www-form-urlencoded' },
            data: { userId: 1, country: code },
            success: () => {
              wx.showToast({ title: '誓言已成', icon: 'success' });
              setTimeout(() => { wx.reLaunch({ url: '/pages/index/index' }); }, 1500);
            }
          });
        }
      }
    });
  }
});