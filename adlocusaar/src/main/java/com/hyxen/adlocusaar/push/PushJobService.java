package com.hyxen.adlocusaar.push;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
//import android.support.annotation.RequiresApi;
import android.util.Log;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PushJobService extends JobService {
    public PushJobService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("PushJobService","-------------onStartJob");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }


}
