package zetbrush.com.generatingmain;

/**
 * Created by Arman on 6/2/15.
 */
public class ProgressHandler implements IProgressCustom{
  int maxinLocal;


    public ProgressHandler(int maxinLocal) {
        this.maxinLocal = maxinLocal;
    }

    public void setMaxinLocal(int maxinLocal) {
        this.maxinLocal = maxinLocal;
    }



    @Override
    public int updateProgress(int x) {

        return (int)((x/100.0)*this.maxinLocal);
    }
}
