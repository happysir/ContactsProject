package com.hdong.broadcast;

import com.hdong.service.PhoneService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * ϵͳ�Ķ���Ĺ㲥
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
