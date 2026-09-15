import { definePreset } from '@primeuix/themes';
import Aura from '@primeuix/themes/aura';

/**
 * `ArgentPreset` — нейтральный «Swiss»-пресет темы PrimeNG, надстроенный
 * поверх стандартного `Aura` через `definePreset`.
 *
 * Как надстроить бренд поверх этого пресета: в конкретном приложении
 * (например, `askshoes-frontend`) вызвать `definePreset(ArgentPreset, {...})`
 * ещё раз и переопределить только нужные семантические токены — ядро при
 * этом не меняется. Для фирменного акцента переопределять нужно ОБА уровня:
 * всю палитру `semantic.primary` (50–950) и `colorScheme.light.primary.*`
 * (`color`/`hoverColor`/`activeColor`/`contrastColor`); если заменить только
 * `primary.color`, `highlight.*`, `tag` и прочие потребители палитры
 * останутся нейтральными (см. `AskShoesPreset` в `askshoes-frontend`).
 */
export const ArgentPreset = definePreset(Aura, {
  primitive: {
    borderRadius: {
      none: '0',
      xs: '0',
      sm: '0',
      md: '0',
      lg: '0',
      xl: '0',
    },
  },
  semantic: {
    // Палитра акцента — нейтральная, как и `colorScheme.light.primary` ниже
    // (в `Aura` здесь `emerald`).
    primary: {
      50: '{neutral.50}',
      100: '{neutral.100}',
      200: '{neutral.200}',
      300: '{neutral.300}',
      400: '{neutral.400}',
      500: '{neutral.500}',
      600: '{neutral.600}',
      700: '{neutral.700}',
      800: '{neutral.800}',
      900: '{neutral.900}',
      950: '{neutral.950}',
    },
    focusRing: {
      width: '2px',
      style: 'solid',
      color: '{primary.color}',
      offset: '1px',
      shadow: 'none',
    },
    overlay: {
      select: {
        shadow: 'none',
      },
      popover: {
        shadow: 'none',
      },
      modal: {
        shadow: 'none',
      },
      // Тень всплывающих меню (`menu`, `tieredmenu` и т.п.) — ключ из
      // `aura/base` (`semantic.overlay.navigation.shadow`).
      navigation: {
        shadow: 'none',
      },
    },
    colorScheme: {
      light: {
        primary: {
          color: '{neutral.900}',
          contrastColor: '#ffffff',
          hoverColor: '{neutral.800}',
          activeColor: '{neutral.700}',
        },
        surface: {
          0: '#ffffff',
          50: '{neutral.50}',
          100: '{neutral.100}',
          200: '{neutral.200}',
          300: '{neutral.300}',
          400: '{neutral.400}',
          500: '{neutral.500}',
          600: '{neutral.600}',
          700: '{neutral.700}',
          800: '{neutral.800}',
          900: '{neutral.900}',
          950: '{neutral.950}',
        },
        text: {
          color: '{surface.900}',
          mutedColor: '{surface.500}',
        },
        overlay: {
          select: {
            borderColor: '{surface.900}',
          },
          popover: {
            borderColor: '{surface.900}',
          },
          modal: {
            borderColor: '{surface.900}',
          },
        },
      },
      // dark намеренно не задаётся: `definePreset` мёрджит только
      // перечисленные ключи, а не заменяет ветку целиком — пустого
      // объекта не нужно, тёмная палитра просто наследуется от `Aura`
      // (см. TSDoc выше) и деактивируется через `darkModeSelector`.
    },
  },
  components: {
    // Ключи сверены с `@primeuix/themes/dist/aura/tabs` (опечатка в имени
    // ключа `definePreset` молча проигнорирует).
    tabs: {
      // Горизонтальные линии даёт каркас (`ShellLayout`), а не сам список
      // вкладок — иначе под шапкой была бы двойная линия.
      tablist: {
        background: 'transparent',
        borderWidth: '0',
      },
      // Вкладки разделены слабыми вертикальными линиями; цвет рамки не
      // меняется на hover/active — активность показывает только полоса.
      tab: {
        borderWidth: '0 1px 0 0',
        hoverBorderColor: '{content.border.color}',
        activeBorderColor: '{content.border.color}',
        margin: '0',
      },
      // Активная вкладка — полоса 3px цвета `{primary.color}` (в `Aura` 1px).
      activeBar: {
        height: '3px',
        bottom: '0',
      },
      // Стрелки прокрутки: в `Aura` фон `{content.background}` и в light —
      // белая тень-«вуаль» поверх вкладок. Без теней: фон прозрачный.
      navButton: {
        background: 'transparent',
      },
      colorScheme: {
        light: {
          navButton: {
            shadow: 'none',
          },
        },
      },
    },
    // `aura/menu`: рамка всплывающего меню — контрастная, как у остальных
    // оверлеев (тень убрана через `overlay.navigation.shadow`).
    menu: {
      root: {
        borderColor: '{surface.900}',
      },
    },
    // `aura/toast`: тень задана на уровне каждой severity в `colorScheme`.
    toast: {
      colorScheme: {
        light: {
          info: { shadow: 'none' },
          success: { shadow: 'none' },
          warn: { shadow: 'none' },
          error: { shadow: 'none' },
          secondary: { shadow: 'none' },
          contrast: { shadow: 'none' },
        },
      },
    },
    // `aura/toggleswitch`: радиусы — литералы (`30px` и `50%`), поэтому
    // обнуление `primitive.borderRadius` их не затрагивает.
    toggleswitch: {
      root: {
        borderRadius: '0',
      },
      handle: {
        borderRadius: '0',
      },
    },
  },
});
