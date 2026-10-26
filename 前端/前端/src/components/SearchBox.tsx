import type { SearchBoxProps } from "@fluentui/react-components";
import { Field, SearchBox, tokens } from "@fluentui/react-components";
import { forwardRef } from "react";

interface DefaultProps extends SearchBoxProps {
  label: string; // 新增 label 属性
  placeholder: string; // 新增 placeholder 属性
  width?: number | string; // 新增 width 属性
  ref?: React.Ref<HTMLInputElement>; // 新增 ref 属性
}
export const SearchBoxStyle = forwardRef<HTMLInputElement, Omit<DefaultProps, "ref">>((props: DefaultProps, ref) => {
  const { label, placeholder, ...searchBoxProps } = props;
  return (
    <Field label={label} style={{ margin: 0 }}>
      <SearchBox
        {...searchBoxProps}
        ref={ref}
        placeholder={placeholder}
        style={{
          width: "100%",
          maxWidth: 1920,
          marginRight: tokens.spacingHorizontalM,
        }}
      />
    </Field>
  );
});

export default SearchBoxStyle;
