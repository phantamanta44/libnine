package io.github.phantamanta44.libnine.util.render;

public enum DrawOrientation {

    LEFT_TO_RIGHT {
        @Override
        public void draw(TextureRegion texture, int x, int y, float fraction) {
            texture.drawPartial(x, y, 0F, 0F, fraction, 1F);
        }
    },
    BOTTOM_TO_TOP {
        @Override
        public void draw(TextureRegion texture, int x, int y, float fraction) {
            texture.drawPartial(x, y, 0F, 1F - fraction, 1F, 1F);
        }
    },
    RIGHT_TO_LEFT {
        @Override
        public void draw(TextureRegion texture, int x, int y, float fraction) {
            texture.drawPartial(x, y, 1F - fraction, 0F, 1F, 1F);
        }
    },
    TOP_TO_BOTTOM {
        @Override
        public void draw(TextureRegion texture, int x, int y, float fraction) {
            texture.drawPartial(x, y, 0F, 0F, 1F, fraction);
        }
    };

    public abstract void draw(TextureRegion texture, int x, int y, float fraction);

}
