package com.hdong.broadcast;

import com.hdong.service.PhoneService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 系统的定义的广播
 * 
 * @author hdong
 * 
 */
public class PhoneBroadcastReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		context.startService(new Intent(context, PhoneService.class));
	}

}
