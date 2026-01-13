Page({
  data: {
    userId: 1,
    generals: [],
    index: 0,
    selectedGeneralId: null,
    gold: 0,
    unitList: [], // 💡 初始为空，由后端数据动态填充
    currentArmyList: [], 
    showResult: false,
    lastRecruitName: '',
    lastRecruitCount: 0
  },

  onShow() {
    this.fetchProfileAndUnits();
    this.fetchGenerals();
  },

  // 1. 获取玩家存档及所有可用兵种的价格配置
  fetchProfileAndUnits() {
    const that = this;
    wx.request({
      url: 'http://localhost:8888/api/game/profile', // 💡 假设此接口已通过 DTO 包含兵种配置，或调用新接口
      data: { userId: this.data.userId },
      success: (res) => {
        if (res.data) {
          const myCountry = res.data.unlockedCountries;
          
          // 💡 模拟从后端获取兵种配置（实际开发中应请求一个 fetchAllUnits 接口）
          // 假设后端返回的数据结构中包含 unitConfigs
          // 如果后端还没写，可以先用以下逻辑，但价格必须从数据库查询
          let list = [
            { name: 'INFANTRY', label: '常规步兵', cost: 15, buyCount: 100 }, // 💡 同步 DataInit 中的 15
            { name: 'ARCHER', label: '常规弓兵', cost: 22, buyCount: 100 },   // 💡 同步 DataInit 中的 22
            { name: 'CAVALRY', label: '常规骑兵', cost: 45, buyCount: 50 }    // 💡 同步 DataInit 中的 45
          ];

          // 特种兵逻辑保持动态注入，价格同步数据库
          if (myCountry === 'CN') list.push({ name: 'CN_SPECIAL', label: '汉之羽林', cost: 35, buyCount: 50 });
          else if (myCountry === 'JP') list.push({ name: 'JP_SPECIAL', label: '大和武士', cost: 55, buyCount: 50 });
          else if (myCountry === 'KR') list.push({ name: 'KR_SPECIAL', label: '高丽铁骑', cost: 65, buyCount: 50 });
          else if (myCountry === 'GB') list.push({ name: 'GB_SPECIAL', label: '长弓勇士', cost: 40, buyCount: 50 });

          this.setData({ 
            unitList: list, 
            gold: res.data.gold 
          });
        }
      }
    });
  },

  // 2. 招募逻辑：发送准确的单价或由后端自动校验
  doRecruit(e) {
    const unitName = e.currentTarget.dataset.name;
    const count = e.currentTarget.dataset.count;
    const unitObj = this.data.unitList.find(u => u.name === unitName);

    wx.showLoading({ title: '正在招募...' });
    wx.request({
      url: 'http://localhost:8888/api/game/recruit',
      method: 'POST',
      header: { 'content-type': 'application/x-www-form-urlencoded' },
      data: {
        userId: this.data.userId,
        generalId: this.data.selectedGeneralId,
        unitName: unitName,
        count: count
      },
      success: (res) => {
        // 💡 res.data 现在应该是后端 BarracksService 返回的成功/失败字符串
        if (res.data.indexOf("成功") !== -1) {
          this.setData({
            showResult: true,
            lastRecruitName: unitObj ? unitObj.label : '部队',
            lastRecruitCount: count
          });
          this.fetchProfileAndUnits();
          this.fetchGenerals();
        } else {
          wx.showModal({ title: '招募失败', content: res.data, showCancel: false });
        }
      },
      complete: () => { wx.hideLoading(); }
    });
  },

  fetchGenerals() {
    wx.request({
      url: 'http://localhost:8888/api/game/generals',
      data: { userId: this.data.userId },
      success: (res) => {
        if (res.data && res.data.length > 0) {
          const currentGeneral = res.data[this.data.index];
          
          // 💡 核心修复：如果你招募的兵在仓库，就读 reserveArmyConfigStr
          // 如果你招募的兵直接上阵，就读 armyConfigStr
          const jsonStr = currentGeneral.reserveArmyConfigStr || ""; 
          
          console.log("当前解析的兵力字符串:", jsonStr); // 调试用
          const parsed = this.parseArmyJson(jsonStr);
  
          this.setData({
            generals: res.data,
            selectedGeneralId: currentGeneral.id,
            currentArmyList: parsed
          });
        }
      }
    });
  },
  
  parseArmyJson(jsonStr) {
    if (!jsonStr || jsonStr === "" || jsonStr === "{}") return [];
    try {
      const armyObj = JSON.parse(jsonStr);
      const labels = { 'INFANTRY': '步兵', 'ARCHER': '弓兵', 'CAVALRY': '骑兵', 'CN_SPECIAL': '羽林' };
      return Object.keys(armyObj).map(key => ({
        label: labels[key] || key,
        count: armyObj[key]
      }));
    } catch (e) { return []; }
  },
  changeCount(e) {
    const { index, type } = e.currentTarget.dataset;
    let list = this.data.unitList;
    list[index].buyCount = type === 'add' ? list[index].buyCount + 10 : Math.max(0, list[index].buyCount - 10);
    this.setData({ unitList: list });
  },
  inputCount(e) {
    let list = this.data.unitList;
    list[e.currentTarget.dataset.index].buyCount = parseInt(e.detail.value) || 0;
    this.setData({ unitList: list });
  },
  bindGeneralChange(e) { this.setData({ index: e.detail.value }, () => this.fetchGenerals()); },
  closeResult() { this.setData({ showResult: false }); },
  stopBubble() {},
  goBack() { wx.navigateBack(); }
});