package dev.sevennv.client.gui.widget;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.EnumSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.config.KeybindSetting;
import dev.sevennv.client.config.Setting;
import dev.sevennv.client.config.StringSetting;
import dev.sevennv.client.config.Theme;

/** Builds the correct widget for a setting. Unknown setting types are skipped. */
public final class WidgetFactory {
    private WidgetFactory() {
    }

    /** Row height used for the simple one-line widgets. */
    public static final int ROW_HEIGHT = 14;

    public static SettingWidget create(Setting<?> setting, Theme theme, int x, int y, int width) {
        if (setting instanceof BooleanSetting bool) {
            return new BooleanWidget(bool, theme, x, y, width, ROW_HEIGHT);
        }
        if (setting instanceof IntSetting integer) {
            return new SliderWidget(setting.getName(), integer::get, value -> integer.set((int) Math.round(value)),
                    integer.getMin(), integer.getMax(), "", theme, x, y, width, 20);
        }
        if (setting instanceof DoubleSetting number) {
            return new SliderWidget(setting.getName(), number::get, number::set,
                    number.getMin(), number.getMax(), number.getSuffix(), theme, x, y, width, 20);
        }
        if (setting instanceof EnumSetting<?> option) {
            return new EnumWidget(option, theme, x, y, width, ROW_HEIGHT);
        }
        if (setting instanceof KeybindSetting keybind) {
            return new KeybindWidget(keybind, theme, x, y, width, ROW_HEIGHT);
        }
        if (setting instanceof StringSetting text) {
            return new StringWidget(text, theme, x, y, width, 24);
        }
        if (setting instanceof ColorSetting color) {
            return new ColorWidget(color, theme, x, y, width, ROW_HEIGHT);
        }
        return null;
    }
}
