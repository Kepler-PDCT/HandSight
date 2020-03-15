package com.example.handsight;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.camera.core.ImageProxy;


public class ImitationGameActivity extends AbstractCameraXActivity<ImitationGameActivity.AnalysisResult> {

    public static final String INTENT_MODULE_ASSET_NAME = "INTENT_MODULE_ASSET_NAME";
    public static final String INTENT_INFO_VIEW_TYPE = "INTENT_INFO_VIEW_TYPE";

    private static final int INPUT_TENSOR_WIDTH = 224;
    private static final int INPUT_TENSOR_HEIGHT = 224;
    private static final int TOP_K = 3;
    private static final int MOVING_AVG_PERIOD = 10;
    private static final String FORMAT_MS = "%dms";
    private static final String FORMAT_AVG_MS = "avg:%.0fms";

    private static final String FORMAT_FPS = "%.1fFPS";
    public static final String SCORES_FORMAT = "%.2f";

    static class AnalysisResult {

        private final String[] topNClassNames;
        private final float[] topNScores;
        private final long analysisDuration;
        private final long moduleForwardDuration;

        public AnalysisResult(String[] topNClassNames, float[] topNScores,
                              long moduleForwardDuration, long analysisDuration) {
            this.topNClassNames = topNClassNames;
            this.topNScores = topNScores;
            this.moduleForwardDuration = moduleForwardDuration;
            this.analysisDuration = analysisDuration;
        }
    }

    private boolean mAnalyzeImageErrorState;
    //private ResultRowView[] mResultRowViews = new ResultRowView[TOP_K];
    private Module mModule;
    private String mModuleAssetName;
    private FloatBuffer mInputTensorBuffer;
    private Tensor mInputTensor;
    private long mMovingAvgSum = 0;
    private Queue<Long> mMovingAvgQueue = new LinkedList<>();

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_image_classification;
    }

    @Override
    protected TextureView getCameraPreviewTextureView() {
        return ((ViewStub) findViewById(R.id.image_classification_texture_view_stub))
                .inflate()
                .findViewById(R.id.image_classification_texture_view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void applyToUiAnalyzeImageResult(AnalysisResult result) {
        mMovingAvgSum += result.moduleForwardDuration;
        mMovingAvgQueue.add(result.moduleForwardDuration);
        if (mMovingAvgQueue.size() > MOVING_AVG_PERIOD) {
            mMovingAvgSum -= mMovingAvgQueue.remove();
        }

        Log.d("TEST", result.topNClassNames[0]);
        Log.d("TEST", Float.toString(result.topNScores[0]));

    }

    protected String getModuleAssetName() {
        return "android_model_2_softmax.pt";
    }

    @Override
    protected String getInfoViewAdditionalText() {
        return getModuleAssetName();
    }

    @Override
    @WorkerThread
    @Nullable
    protected AnalysisResult analyzeImage(ImageProxy image, int rotationDegrees) {
        if (mAnalyzeImageErrorState) {
            return null;
        }

        try {
            if (mModule == null) {
                final String moduleFileAbsoluteFilePath = new File(
                        Utils.assetFilePath(this, getModuleAssetName())).getAbsolutePath();
                mModule = Module.load(moduleFileAbsoluteFilePath);

                mInputTensorBuffer =
                        Tensor.allocateFloatBuffer(3 * INPUT_TENSOR_WIDTH * INPUT_TENSOR_HEIGHT);
                mInputTensor = Tensor.fromBlob(mInputTensorBuffer, new long[]{1, 3, INPUT_TENSOR_HEIGHT, INPUT_TENSOR_WIDTH});
            }

            final long startTime = SystemClock.elapsedRealtime();
            TensorImageUtils.imageYUV420CenterCropToFloatBuffer(
                    image.getImage(), rotationDegrees,
                    INPUT_TENSOR_WIDTH, INPUT_TENSOR_HEIGHT,
                    TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                    TensorImageUtils.TORCHVISION_NORM_STD_RGB,
                    mInputTensorBuffer, 0);

            final long moduleForwardStartTime = SystemClock.elapsedRealtime();
            final Tensor outputTensor = mModule.forward(IValue.from(mInputTensor)).toTensor();
            final long moduleForwardDuration = SystemClock.elapsedRealtime() - moduleForwardStartTime;

            final float[] scores = outputTensor.getDataAsFloatArray();
            final int[] ixs = Utils.topK(scores, TOP_K);

            final String[] topKClassNames = new String[TOP_K];
            final float[] topKScores = new float[TOP_K];
            for (int i = 0; i < TOP_K; i++) {
                final int ix = ixs[i];
                topKClassNames[i] = Constants.IMAGENET_CLASSES[ix];
                topKScores[i] = scores[ix];
            }
            final long analysisDuration = SystemClock.elapsedRealtime() - startTime;
            return new AnalysisResult(topKClassNames, topKScores, moduleForwardDuration, analysisDuration);
        } catch (Exception e) {
            Log.e(Constants.TAG, "Error during image analysis", e);
            mAnalyzeImageErrorState = true;
            runOnUiThread(() -> {
                if (!isFinishing()) {
                    Log.d("TEST", "is finishing");
                }
            });
            return null;
        }
    }

    @Override
    protected int getInfoViewCode() {
        return getIntent().getIntExtra(INTENT_INFO_VIEW_TYPE, -1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mModule != null) {
            mModule.destroy();
        }
    }
}
