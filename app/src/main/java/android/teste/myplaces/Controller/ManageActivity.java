package android.teste.myplaces.Controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.teste.myplaces.R;
import android.os.Bundle;
import android.teste.myplaces.Util.SingletonDatabase;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ManageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

        LinearLayout ll1 = findViewById(R.id.layoutConteudoMng);
        LinearLayout ll2 = findViewById(R.id.layoutDeletar);

        try {
            Cursor c = SingletonDatabase.getInstance().buscar("Checkin", new String[]{"Local"}, "", "");
            while(c.moveToNext()){
                TextView tvLoc = new TextView(this);

                String name = c.getString(c.getColumnIndex("Local"));
                tvLoc.setText(name);
                tvLoc.setGravity(Gravity.CENTER);

                ViewGroup.LayoutParams layoutParams1 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams1.height = getResources().getDimensionPixelSize(R.dimen.listItemHeight);
                layoutParams1.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                tvLoc.setLayoutParams(layoutParams1);

                ll1.addView(tvLoc);

                ImageButton delBtn = new ImageButton(this);

                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                delBtn.setTag(c.getString(c.getColumnIndex("Local")));
                delBtn.setImageResource(R.drawable.ic_baseline_delete_24);
                delBtn.setBackgroundColor(getResources().getColor(R.color.colorGrey));

                layoutParams2.height = getResources().getDimensionPixelSize(R.dimen.listItemHeight);
                layoutParams2.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                layoutParams2.gravity = Gravity.RIGHT;
                delBtn.setLayoutParams(layoutParams2);

                delBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteCheckin(v);
                    }
                });

                ll2.addView(delBtn);
            }
            c.close();

        } catch (Exception e){
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toastError), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteCheckin(final View v){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.modalDeleteCheckinTitle));
        builder.setMessage(String.format(getResources().getString(R.string.modalDeleteCheckinMsg), v.getTag()));

        builder.setPositiveButton(R.string.modalYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try{
                    SingletonDatabase.getInstance().deletar("Checkin", "Local = '" + v.getTag() + "'");
                    recreate();
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.toastChangesSuccess), Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.toastError), Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton(R.string.modalNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.manage_act_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.optManageBack:
                Intent it = getIntent();
                setResult(2, it);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
