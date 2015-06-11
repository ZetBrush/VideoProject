package zetbrush.com.view;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.picsartvideo.R;

public enum FragmentProvider {


    /*CUSTOM_TAB_ICONS(R.string.demo_title_custom_tab_icons, R.layout.demo_custom_tab_icons) {
        @Override
        public int[] tabs() {
            return new int[] {
                    R.string.demo_tab_no_title,
                    R.string.demo_tab_no_title,
                    R.string.demo_tab_no_title,
                    R.string.demo_tab_no_title
            };
        }
        @Override
        public void setup(SmartTabLayout layout) {
            super.setup(layout);

            final LayoutInflater inflater = LayoutInflater.from(layout.getContext());
            final Resources res = layout.getContext().getResources();

            layout.setCustomTabView(new SmartTabLayout.TabProvider() {
                @Override
                public View createTabView(ViewGroup container, int position, PagerAdapter adapter) {
                    ImageView icon = (ImageView) inflater.inflate(R.layout.custom_tab_icon, container, false);
                    switch (position) {
                        case 0:
                            icon.setImageDrawable(res.getDrawable(R.drawable.ic_home_white_24dp));
                            break;
                        case 1:
                            icon.setImageDrawable(res.getDrawable(R.drawable.ic_search_white_24dp));
                            break;
                        case 2:
                            icon.setImageDrawable(res.getDrawable(R.drawable.ic_person_white_24dp));
                            break;
                        case 3:
                            icon.setImageDrawable(res.getDrawable(R.drawable.ic_flash_on_white_24dp));
                            break;
                        default:
                            throw new IllegalStateException("Invalid position: " + position);
                    }
                    return icon;
                }
            });
        }
    },*/


    INDICATOR_TRICK1(R.string.demo_title_indicator_trick1, R.layout.indicator_trick);

    public static int[] tab10() {
        return new int[]{
                R.string.tab_1,
                R.string.tab_2,
                R.string.tab_3,

        };
    }

    public final int titleResId;
    public final int layoutResId;

    FragmentProvider(int titleResId, int layoutResId) {
        this.titleResId = titleResId;
        this.layoutResId = layoutResId;
    }



    public void setup(final SmartTabLayout layout) {
        //Do nothing.
    }

    public int[] tabs() {
        return tab10();
    }





}
