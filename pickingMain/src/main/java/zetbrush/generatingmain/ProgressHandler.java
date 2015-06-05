package zetbrush.generatingmain;

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
        if(x<1) x=1;
        int a = (int)((this.maxinLocal*1.0)/(x));

        return a;
    }
}
