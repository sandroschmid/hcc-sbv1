// This script displays a sine/cosine table in a window.

   rt = new ResultsTable();
   for (i=0; i*0.1<=2*Math.PI; i++) {
      n = i*0.1;
      rt.setValue("n", i, n);
      rt.setValue("Sine(n)", i, Math.sin(n));
      rt.setValue("Cos(n)", i, Math.cos(n));
   }
   rt.showRowNumbers(false);
   rt.show("Sine/Cosine Table");

