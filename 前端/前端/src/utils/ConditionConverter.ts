import type { ICondition } from "../types/IScript";

function ConditionsToArrayConverter(conds: any): ICondition[] {
  if (!conds) return [];

  // 如果是数组，过滤掉无效项（无 type 或 type 不是字符串）
  if (Array.isArray(conds)) return conds.filter((c) => c && typeof (c as any).type === "string") as ICondition[];

  if (typeof conds === "string") {
    try {
      const parsed = JSON.parse(conds);
      if (Array.isArray(parsed)) return parsed.filter((c) => c && typeof (c as any).type === "string");
      if (parsed && typeof parsed === "object" && typeof (parsed as any).type === "string")
        return [parsed as ICondition];
      return [];
    } catch {
      return [];
    }
  }

  if (typeof conds === "object" && typeof (conds as any).type === "string") return [conds as ICondition];
  return [];
}

export function StringDisplayToKeyConverter(s?: string, placeholders?: Record<string, string> | null) {
  if (!s) return s;
  // placeholders: key(english)->value(chinese)
  const reverse = placeholders
    ? Object.fromEntries(Object.entries(placeholders).map(([k, v]) => [v, k]))
    : ({} as Record<string, string>);
  return s.replace(/\{([^}]+)\}/g, (match, inner) => {
    const key = reverse[inner];
    return key ? `{${key}}` : match;
  });
}

export function StringKeyToDisplayConverter(s?: string, placeholders?: Record<string, string> | null) {
  if (!s) return s;
  return s.replace(/\{([^}]+)\}/g, (match, inner) => {
    const value = placeholders?.[inner];
    return value ? `{${value}}` : match;
  });
}

export function ConditionsDisplayToKeyConverter(
  conds: any,
  placeholders?: Record<string, string> | null
): ICondition[] {
  const arr = ConditionsToArrayConverter(conds);
  return arr.map((c) => {
    if (c.type === "ATOMIC") {
      return {
        ...c,
        lhs: StringDisplayToKeyConverter(c.lhs as string, placeholders),
        rhs: StringDisplayToKeyConverter(c.rhs as string, placeholders),
      };
    } else {
      return { ...c, sub: ConditionsDisplayToKeyConverter(c.sub, placeholders) } as ICondition;
    }
  });
}

export function ConditionsKeyToDisplayConverter(
  conds: any,
  placeholders?: Record<string, string> | null
): ICondition[] {
  const arr = ConditionsToArrayConverter(conds);
  return arr.map((c) => {
    if (c.type === "ATOMIC") {
      return {
        ...c,
        lhs: StringKeyToDisplayConverter(c.lhs as string, placeholders),
        rhs: StringKeyToDisplayConverter(c.rhs as string, placeholders),
      };
    } else {
      return { ...c, sub: ConditionsKeyToDisplayConverter(c.sub, placeholders) } as ICondition;
    }
  });
}
