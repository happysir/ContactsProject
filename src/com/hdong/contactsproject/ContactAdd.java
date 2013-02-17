package com.hdong.contactsproject;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * 添加通讯录联系人，添加事务操作
 * 
 * @author hdong
 * 
 */
public class ContactAdd extends Activity
{
	private EditText add_Name = null;
	private EditText add_Number = null;
	private Button btn_add_cancel = null;
	private Button btn_add_ok = null;

	private String name = null;
	private String phoneNumber = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.contact_add);
		ContactAdd.this.setTitle("添加联系人");

		this.add_Name = (EditText) this.findViewById(R.id.add_Name);
		this.add_Number = (EditText) this.findViewById(R.id.add_Number);
		this.btn_add_cancel = (Button) this.findViewById(R.id.btn_add_cancel);
		this.btn_add_ok = (Button) this.findViewById(R.id.btn_add_ok);

		this.btn_add_cancel.setOnClickListener(clickListener);
		this.btn_add_ok.setOnClickListener(clickListener);
	}

	OnClickListener clickListener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
			case R.id.btn_add_cancel:
				ContactAdd.this.finish();
				break;

			case R.id.btn_add_ok:
				try
				{
					ContactAdd.this.addContact();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				ContactAdd.this.finish();
				break;

			default:
				break;
			}
		}
	};

	void init()
	{
		this.name = this.add_Name.getText().toString();
		this.phoneNumber = this.add_Number.getText().toString();
	}

	private void addContact() throws Exception
	{
		this.init();

		Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
		ContentResolver resolver = this.getContentResolver();
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		// account_name
		ContentProviderOperation cpo = ContentProviderOperation.newInsert(uri)
				.withValue("account_name", null).build();
		operations.add(cpo);

		uri = Uri.parse("content://com.android.contacts/data");

		// 姓名
		ContentProviderOperation cpo2 = ContentProviderOperation
				.newInsert(uri)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				.withValue(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
				.withValue(
						ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
						this.name).build();
		operations.add(cpo2);

		// 电话模式
		ContentProviderOperation cpo3 = ContentProviderOperation
				.newInsert(uri)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				.withValue(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
						this.phoneNumber)
				.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, 1)
				.build();
		operations.add(cpo3);

		ContentProviderOperation cpo4 = ContentProviderOperation.newInsert(uri)
				.withValueBackReference("raw_contact_id", 0)
				.withValue("mimetype", "vnd.android.cursor.item/email_v2")
				.withValue("data1", "").withValue("data2", "2").build();
		operations.add(cpo4);

		resolver.applyBatch("com.android.contacts", operations);
	}
}
