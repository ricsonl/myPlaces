package android.teste.myplaces.Controller;

import android.database.Cursor;
import android.teste.myplaces.R;
import android.os.Bundle;
import android.teste.myplaces.Util.SingletonDatabase;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ReportActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        LinearLayout ll1 = findViewById(R.id.layoutConteudoRep);
        LinearLayout ll2 = findViewById(R.id.layoutVisitas);

        try{
            Cursor c = SingletonDatabase.getInstance().buscar("Checkin", new String[]{"Local", "qtdVisitas"}, "", "");
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

                TextView tvQtd = new TextView(this);
                String qtd = c.getString(c.getColumnIndex("qtdVisitas"));
                tvQtd.setText(qtd);
                tvQtd.setGravity(Gravity.CENTER);

                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams2.height = getResources().getDimensionPixelSize(R.dimen.listItemHeight);
                layoutParams2.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                layoutParams2.gravity = Gravity.RIGHT;
                tvQtd.setLayoutParams(layoutParams2);

                ll2.addView(tvQtd);
            }
            c.close();

        } catch (Exception e){
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toastError), Toast.LENGTH_SHORT).show();
        }
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
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
