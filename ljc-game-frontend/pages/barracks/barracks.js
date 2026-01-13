Page({
  data: {
    userId: 1,
    generals: [],
    index: 0,
    selectedGeneralId: null,
    gold: 0,
    unitList: [],
    currentArmyList: [], 
    showResult: false,
    lastRecruitName: '',
    lastRecruitCount: 0
  },

  onShow() {
    this.fetchProfileAndUnits();
    this.fetchGenerals();
  },

  fetchProfileAndUnits() {
    wx.request({
      url: 'http://localhost:8888/api/game/profile',
      data: { userId: this.data.userId },
      success: (res) => {
        if (res.data) {
          const myCountry = res.data.unlockedCountries;
          let list = [
            { name: 'INFANTRY', label: '常规步兵', cost: 10, buyCount: 100 },
            { name: 'ARCHER', label: '常规弓兵', cost: 10, buyCount: 100 },
            { name: 'CAVALRY', label: '常规骑兵', cost: 20, buyCount: 50 }
          ];
          if (myCountry === 'CN') list.push({ name: 'CN_SPECIAL', label: '汉之羽林', cost: 30, buyCount: 50 });
          else if (myCountry === 'JP') list.push({ name: 'JP_SPECIAL', label: '大和武士', cost: 30, buyCount: 50 });
          else if (myCountry === 'KR') list.push({ name: 'KR_SPECIAL', label: '高丽铁骑', cost: 45, buyCount: 50 });
          else if (myCountry === 'GB') list.push({ name: 'GB_SPECIAL', label: '长弓勇士', cost: 40, buyCount: 50 });
          this.setData({ unitList: list, gold: res.data.gold });
        }
      }
    });
  },

  fetchGenerals() {
    wx.request({
      url: 'http://localhost:8888/api/game/generals',
      data: { userId: this.data.userId },
      success: (res) => {
        if (res.data && res.data.length > 0) {
          const currentGeneral = res.data[this.data.index];
          // 💡 重点：这里改为读取后端字段 armyConfigStr
          const jsonStr = currentGeneral.armyConfigStr || "";
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

  // 💡 适配你后端 Army.toJson() 结构的解析器
  parseArmyJson(jsonStr) {
    if (!jsonStr || jsonStr === "" || jsonStr === "{}") return [];
    try {
      const armyObj = JSON.parse(jsonStr); // 解析后端 JSON 字符串
      const labels = {
        'INFANTRY': '步兵', 'ARCHER': '弓兵', 'CAVALRY': '骑兵',
        'CN_SPECIAL': '羽林', 'JP_SPECIAL': '武士', 'KR_SPECIAL': '铁骑', 'GB_SPECIAL': '长弓'
      };
      // 将 JSON 对象转为展示数组
      return Object.keys(armyObj).map(key => ({
        label: labels[key] || key,
        count: armyObj[key]
      }));
    } catch (e) {
      console.error("JSON解析失败:", e);
      return [];
    }
  },

  doRecruit(e) {
    const unitName = e.currentTarget.dataset.name;
    const count = e.currentTarget.dataset.count;
    const unitLabel = this.data.unitList.find(u => u.name === unitName).label;

    wx.showLoading({ title: '正在招募' });
    wx.request({
      url: 'http://localhost:8888/api/game/recruit',
      method: 'POST',
      header: { 'content-type': 'application/x-www-form-urlencoded' },
      data: { userId: this.data.userId, generalId: this.data.selectedGeneralId, unitName: unitName, count: count },
      success: (res) => {
        this.setData({ showResult: true, lastRecruitName: unitLabel, lastRecruitCount: count });
        this.fetchProfileAndUnits();
        this.fetchGenerals();
      },
      complete: () => wx.hideLoading()
    });
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

  bindGeneralChange(e) {
    this.setData({ index: e.detail.value }, () => this.fetchGenerals());
  },
  closeResult() { this.setData({ showResult: false }); },
  stopBubble() {},
  goBack() { wx.navigateBack(); }
});