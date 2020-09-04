package com.dappergeek0.uplanit;

import android.content.Context;
import android.content.Intent;

/**
 * Created by DapperGeek0 on 3/15/2017.
 */

public class OpenWebLinks {

   private static Context _context;

   public static void openWebPage(Context context, String webUrl, String title){

      _context = context;
      Intent openTerms = new Intent(_context, WebViewActivity.class);
      openTerms.putExtra(Constants.openUrl, webUrl);
      openTerms.putExtra(Constants.urlTitle, title);
      _context.startActivity(openTerms);
   }
}
