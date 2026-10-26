import { type CalendarStrings, defaultDatePickerStrings } from "@fluentui/react-datepicker-compat";

export const localizedStrings: CalendarStrings = {
  ...defaultDatePickerStrings,
  days: ["周日", "周一", "周二", "周三", "周四", "周五", "周六"],
  shortDays: ["日", "一", "二", "三", "四", "五", "六"],
  months: ["1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"],

  shortMonths: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
  goToToday: "转到今日",
};

export function onFormatDate(date?: Date): string {
  return !date ? "" : `${date.getFullYear()}年${localizedStrings.months[date.getMonth()]}${date.getDate()}日`;
}

export function formatDate(date: Date | null): string | null {
  if (date == null) {
    return null;
  }
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}
