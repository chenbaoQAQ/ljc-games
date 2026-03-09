export const CIV_BASE_TROOP = {
  CN: 2000,
  JP: 2100,
  KR: 2200,
  GB: 2300,
};

export const TROOP_CATALOG = {
  1001: { troopId: 1001, civ: "CN", type: "INF", name: "义勇兵", icon: "⚔️", color: "var(--inf-color)", recruitGold: 10, capCost: 1, isElite: false },
  2001: { troopId: 2001, civ: "CN", type: "INF", name: "重盾步兵", icon: "🛡️", color: "var(--inf-color)", recruitGold: 22, capCost: 2, isElite: false },
  2002: { troopId: 2002, civ: "CN", type: "ARC", name: "强弩兵", icon: "🏹", color: "var(--arc-color)", recruitGold: 24, capCost: 2, isElite: false },
  2003: { troopId: 2003, civ: "CN", type: "CAV", name: "虎豹骑", icon: "🐎", color: "var(--cav-color)", recruitGold: 42, capCost: 3, isElite: false },

  2101: { troopId: 2101, civ: "JP", type: "INF", name: "足轻", icon: "🛡️", color: "var(--inf-color)", recruitGold: 22, capCost: 2, isElite: false },
  2102: { troopId: 2102, civ: "JP", type: "ARC", name: "弓足轻", icon: "🏹", color: "var(--arc-color)", recruitGold: 26, capCost: 2, isElite: false },
  2103: { troopId: 2103, civ: "JP", type: "CAV", name: "骑马武者", icon: "🐎", color: "var(--cav-color)", recruitGold: 45, capCost: 3, isElite: false },

  2201: { troopId: 2201, civ: "KR", type: "INF", name: "步卒", icon: "🛡️", color: "var(--inf-color)", recruitGold: 23, capCost: 2, isElite: false },
  2202: { troopId: 2202, civ: "KR", type: "ARC", name: "长弓手", icon: "🏹", color: "var(--arc-color)", recruitGold: 25, capCost: 2, isElite: false },
  2203: { troopId: 2203, civ: "KR", type: "CAV", name: "重骑卒", icon: "🐎", color: "var(--cav-color)", recruitGold: 44, capCost: 3, isElite: false },

  2301: { troopId: 2301, civ: "GB", type: "INF", name: "长枪步兵", icon: "🛡️", color: "var(--inf-color)", recruitGold: 24, capCost: 2, isElite: false },
  2302: { troopId: 2302, civ: "GB", type: "ARC", name: "长弓手", icon: "🏹", color: "var(--arc-color)", recruitGold: 28, capCost: 2, isElite: false },
  2303: { troopId: 2303, civ: "GB", type: "CAV", name: "重骑士", icon: "🐎", color: "var(--cav-color)", recruitGold: 46, capCost: 3, isElite: false },

  3001: { troopId: 3001, civ: "CN", type: "ARC", name: "青囊医官", icon: "💊", color: "#10b981", recruitGold: 130, capCost: 3, isElite: true },
  3002: { troopId: 3002, civ: "JP", type: "ARC", name: "爆裂火筒队", icon: "🔥", color: "#f97316", recruitGold: 145, capCost: 3, isElite: true },
  3003: { troopId: 3003, civ: "KR", type: "INF", name: "军乐旗卫", icon: "🎺", color: "#06b6d4", recruitGold: 140, capCost: 3, isElite: true },
  3004: { troopId: 3004, civ: "GB", type: "INF", name: "破甲工兵", icon: "🪓", color: "#a3a3a3", recruitGold: 150, capCost: 3, isElite: true },
};

export const EQUIPMENT_SLOT_LABEL = {
  weapon: "武器",
  armor1: "防具",
  helm: "头盔",
  boots: "鞋子",
  mount: "坐骑",
  accessory: "饰品",
};

export const SKILL_BOOK_META = {
  301: { name: "鼓舞技能书" },
  302: { name: "战地医治技能书" },
  303: { name: "毒箭技能书" },
  304: { name: "威压技能书" },
  305: { name: "铁壁技能书" },
  306: { name: "一石二鸟技能书" },
  307: { name: "偷袭技能书" },
  308: { name: "红颜克制技能书" },
  309: { name: "铁躯技能书" },
  310: { name: "易伤掌控技能书" },
  311: { name: "反震技能书" },
  312: { name: "先机技能书" },
  313: { name: "避锋技能书" },
};

export function getTroopMeta(troopId) {
  return TROOP_CATALOG[troopId] || {
    troopId,
    civ: "",
    type: "INF",
    name: `兵种#${troopId}`,
    icon: "⚔️",
    color: "var(--text-secondary)",
    recruitGold: 20,
    capCost: 1,
    isElite: false,
  };
}

export function getTroopTypeName(type) {
  const map = { INF: "步兵", ARC: "弓兵", CAV: "骑兵" };
  return map[type] || type;
}

export function getEquipmentSlotLabel(slot) {
  return EQUIPMENT_SLOT_LABEL[slot] || "装备";
}

export function getEquipmentSlotIcon(slot) {
  if (slot === "weapon") return "⚔️";
  if (slot === "armor1") return "🛡️";
  if (slot === "helm") return "🪖";
  if (slot === "boots") return "👢";
  if (slot === "mount") return "🐎";
  if (slot === "accessory") return "💍";
  return "📦";
}
