package com.hdong.contactsproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hdong.contactsproject.R;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/**
 * �����棬��ϵ���б�
 * 
 * @author hdong
 * 
 */
public class MainActivity extends Activity
{
	private Cursor result = null;
	private ListView contactsList = null;
	public static SimpleAdapter simpleAdapter = null;
	public List<Map<String, Object>> allContacts = null;
	private boolean flag = true; // ȫ�ֱ������Ż�������
	String contactsId = null; // ѡ���λ��
	public int delPostion = 0; // ��Ҫɾ����item���

	public static MainActivity instance = null; // ����һ����̬�������ı�������onCreateʱ��ֵthis������ContactInfo��Ĺر�ʱ����

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		instance = this;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.setTitle("�ҵ�ͨѶ¼");
	}

	@Override
	protected void onResume()
	{
		this.contactsList = (ListView) this.findViewById(R.id.contactList);

		this.result = this.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, "sort_key asc");
		this.startManagingCursor(this.result); // �������������������
		this.allContacts = new ArrayList<Map<String, Object>>();

		for (this.result.moveToFirst(); !this.result.isAfterLast(); this.result.moveToNext())
		{
			Map<String, Object> contact = new HashMap<String, Object>();
			contact.put("_id",
					this.result.getInt(this.result.getColumnIndex(ContactsContract.Contacts._ID)));
			contact.put("name", this.result.getString(this.result
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
			this.allContacts.add(contact);
		}

		this.simpleAdapter = new SimpleAdapter(this, this.allContacts, R.layout.contacts,
				new String[] { "name" }, new int[] { R.id.name });
		this.contactsList.setAdapter(simpleAdapter);
		this.contactsList.setOnItemClickListener(itemClickListener); // �����ϵ���б���ĵ���¼�����
		this.registerForContextMenu(this.contactsList); // ע��˵�

		// ****************************************************************
		Intent intent = this.getIntent();
		boolean isDelete = intent.getBooleanExtra("isDelete", false);
		System.out.println("isDelete = " + isDelete);
		if (isDelete)
		{
			this.allContacts.remove(0);
			this.simpleAdapter.notifyDataSetChanged(); // ����ˢ��
		}
		// ****************************************************************
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// getMenuInflater().inflate(R.menu.activity_main, menu); �Դ���settings��
		menu.add(0, Menu.FIRST + 1, 1, "����Wo");
		menu.add(0, Menu.FIRST + 2, 1, "���");
		menu.add(0, Menu.FIRST + 3, 1, "�˳�");
		return true; // ����false����Menu��ʧЧ
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case Menu.FIRST + 1: // ����
			break;

		case Menu.FIRST + 2: // ���
			Intent intent = new Intent(MainActivity.this, ContactAdd.class);
			this.startActivity(intent);
			break;

		case Menu.FIRST + 3: // �˳�
			this.finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) // �����˵�
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("��ϵ�˲���");
		menu.add(Menu.FIRST, Menu.FIRST + 1, 1, "�鿴����");
		menu.add(Menu.FIRST, Menu.FIRST + 2, 1, "ɾ����ϵ��");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		this.delPostion = info.position; // ȡ�ò���Ϊ����ͨ��intent���ݸ�ContactInfo�У�����ɾ������
		contactsId = this.allContacts.get(this.delPostion).get("_id") // ͨ��get()������map��ȡֵ
				.toString();

		switch (item.getItemId())
		// ���в˵�����
		{
		case Menu.FIRST + 1: // �鿴
			Intent intent = new Intent("information");

			String phoneSelection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
			String[] phoneSelectionArgs = new String[] { contactsId };
			Cursor cursor = this.getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, phoneSelection,
					phoneSelectionArgs, null);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
			{
				intent.putExtra("number", cursor.getString(cursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
				intent.putExtra("name", cursor.getString(cursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
			}
			intent.putExtra("_id", contactsId);
			MainActivity.this.startActivity(intent);
			break;

		case Menu.FIRST + 2: // ɾ��
			new AlertDialog.Builder(this).setTitle("ɾ����ʾ").setIcon(null).setMessage("��ȷ��Ҫɾ����")
					.setPositiveButton("ȷ��", new OnClickListener()
					{

						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							// ȷ����ť�ļ����¼�����
							MainActivity.this.getContentResolver().delete(
									Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,
											MainActivity.this.contactsId), null, null);
							MainActivity.this.allContacts.remove(MainActivity.this.delPostion); // ɾ������������
							MainActivity.this.simpleAdapter.notifyDataSetChanged(); // ֪ͨ�ı�
						}
					}).setNegativeButton("ȡ��", new OnClickListener()
					{

						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							// ȡ����ť���¼���������

						}
					}).show();
			break;

		default:
			break;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) // ÿ�ΰ������ʼ��
	{

		if (keyCode == KeyEvent.KEYCODE_BACK && flag)
		{
			flag = false;
			Toast.makeText(this, "�ٵ��һ���˳�����", Toast.LENGTH_LONG).show();
			return false;
		}
		else
		{
			return super.onKeyDown(keyCode, event);
		}
	}

	OnItemClickListener itemClickListener = new OnItemClickListener() // item�ĵ���¼�
	{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			String contactsId = MainActivity.this.allContacts.get(position).get("_id").toString();

			Intent intent = new Intent(MainActivity.this, ContactInfo.class);
			String phoneSelection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
			String[] phoneSelectionArgs = new String[] { contactsId };
			Cursor cursor = MainActivity.this.getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, phoneSelection,
					phoneSelectionArgs, null);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
			{
				intent.putExtra("number", cursor.getString(cursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
				intent.putExtra("name", cursor.getString(cursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
			}
			intent.putExtra("_id", contactsId);
			MainActivity.this.startActivity(intent);
		}
	};

}
