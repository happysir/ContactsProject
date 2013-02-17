package com.hdong.contactsproject;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * ��ϵ����ϸ�鿴����
 * 
 * @author hdong
 * 
 */
public class ContactInfo extends Activity
{
	private TextView titleInfo = null;
	private TextView phoneNumber = null;
	private ImageButton telButton = null;
	private ImageButton semButton = null;

	public String id = null; // id
	public String name = null; // ����
	public String number = null; // �绰����

	public boolean isDelete = false; // ����Ƿ�ɾ��

	OnClickListener clickListener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
			case R.id.but_tel:
				Uri uri = Uri.parse("tel:" + number);
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_CALL); // ֱ�Ӳ��ţ�����������Ԥ����
				intent.setData(uri);
				ContactInfo.this.startActivity(intent);
				break;

			case R.id.but_sms:
				Intent intent2 = new Intent();
				intent2.setAction(Intent.ACTION_VIEW);
				intent2.setType("vnd.android-dir/mms-sms");
				ContactInfo.this.startActivity(intent2);
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.contact_info);
		this.setTitle("�鿴��ϵ��");

	}

	@Override
	protected void onResume()
	{
		Intent intent = this.getIntent();
		this.titleInfo = (TextView) this.findViewById(R.id.titleInfo);
		name = intent.getStringExtra("name");
		this.titleInfo.setText(name + "\n\t");

		// ��ȡidֵ
		id = intent.getStringExtra("_id");
		this.phoneNumber = (TextView) this.findViewById(R.id.phoneNumber);
		number = intent.getStringExtra("number");
		this.phoneNumber.setText(number.trim());

		this.telButton = (ImageButton) this.findViewById(R.id.but_tel); // ����绰��ͼƬ��ť
		this.semButton = (ImageButton) this.findViewById(R.id.but_sms); // ���Ͷ��ŵ�ͼƬ��ť

		this.telButton.setOnClickListener(clickListener);
		this.semButton.setOnClickListener(clickListener);
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0, Menu.FIRST + 1, 1, "ɾ��");
		menu.add(0, Menu.FIRST + 2, 1, "�༭");
		menu.add(0, Menu.FIRST + 3, 1, "�˳�");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case Menu.FIRST + 3: // �˳�
			System.out.println(MainActivity.instance.isFinishing()
					+ "-----------1");
			// ����֮ǰ���и�activity������Ҫʵ��ֱ���˳��Ĺ��ܣ��͵ùر����activity֮��Ҳ�ر�ǰ���Ǹ�activity
			this.finish();
			MainActivity.instance.finish();
			System.out.println(MainActivity.instance.isFinishing()
					+ "-----------2");
			break;

		case Menu.FIRST + 2: // �༭
			Intent intent = new Intent(ContactInfo.this, ContactEditInfo.class);
			// intent.setAction("ContactEditInfo");
			intent.putExtra("id", id);
			intent.putExtra("name1", name);
			intent.putExtra("number1", number);
			this.startActivity(intent);
			break;

		case Menu.FIRST + 1: // ɾ��
			new AlertDialog.Builder(ContactInfo.this)
					.setTitle("ɾ������")
					.setIcon(null)
					.setMessage("��ȷ��Ҫɾ����")
					.setNegativeButton(
							"ȡ��",
							new android.content.DialogInterface.OnClickListener()
							{

								@Override
								public void onClick(DialogInterface dialog,
										int which)
								{

								}
							})
					.setPositiveButton(
							"ȷ��",
							new android.content.DialogInterface.OnClickListener()
							{

								@Override
								public void onClick(DialogInterface dialog,
										int which)
								{
									try
									{
										ContactInfo.this.deleteContact();
										ContactInfo.this.isDelete = true;
										Intent intent = new Intent(
												ContactInfo.this,
												MainActivity.class);
										intent.putExtra("isDelete",
												ContactInfo.this.isDelete);
										ContactInfo.this.startActivity(intent);
									}
									catch (Exception e)
									{
										e.printStackTrace();
									}
									ContactInfo.this.finish();
								}
							}).show();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void deleteContact() throws Exception
	{
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		Uri uri = Uri.parse("content://com.android.contacts/data");
		ContentProviderOperation cpo = ContentProviderOperation
				.newDelete(uri)
				.withSelection(Data.CONTACT_ID + "=?", new String[] { this.id })
				.build();
		operations.add(cpo);

		this.getContentResolver().applyBatch(ContactsContract.AUTHORITY,
				operations);
	}

}
