package computician.janusclientapi;

/**
 * Created by ben.trent on 6/25/2015.
 */
public class JanusMediaConstraints {

    public class JanusVideo {
        private int maxHeight, minHeight, maxWidth, minWidth, maxFramerate, minFramerate;
        public JanusVideo()
        {
            maxFramerate = 15;
            minFramerate = 0;
            maxHeight = 240;
            minHeight = 0;
            maxWidth = 320;
            minWidth = 0;
        }
        public int getMaxHeight() {
            return maxHeight;
        }

        public void setMaxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
        }

        public int getMinHeight() {
            return minHeight;
        }

        public void setMinHeight(int minHeight) {
            this.minHeight = minHeight;
        }

        public int getMaxWidth() {
            return maxWidth;
        }

        public void setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
        }

        public int getMinWidth() {
            return minWidth;
        }

        public void setMinWidth(int minWidth) {
            this.minWidth = minWidth;
        }

        public int getMaxFramerate() {
            return maxFramerate;
        }

        public void setMaxFramerate(int maxFramerate) {
            this.maxFramerate = maxFramerate;
        }

        public int getMinFramerate() {
            return minFramerate;
        }

        public void setMinFramerate(int minFramerate) {
            this.minFramerate = minFramerate;
        }
    }

    public class JanusAudio {
        public JanusAudio() {}
    }

    private JanusAudio audio = null;

    private JanusVideo video = null;

    public JanusMediaConstraints() { }

    public JanusVideo getVideo() {
        return video;
    }

    public void setVideo(JanusVideo video) {
        this.video = video;
    }

    public JanusAudio getAudio() {
        return audio;
    }

    public void setAudio(JanusAudio audio) {
        this.audio = audio;
    }

}
