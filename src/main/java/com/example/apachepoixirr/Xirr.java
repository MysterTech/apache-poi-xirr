package com.example.apachepoixirr;

public class Xirr {

  private static final double tol = 0.00000001;

  private static double f_xirr(double p, double dt, double dt0, double x) {
    double resf = p * Math.pow((x + 1d), (dt0 - dt) / 365d);
    return resf;
  }

  private static double df_xirr(double p, double dt, double dt0, double x) {
    double resf = (1d / 365d) * (dt0 - dt) * p * Math.pow((x + 1d), ((dt0 - dt) / 365d) - 1d);
    return resf;
  }

  private static double total_f_xirr(double[] payments, double[] days, double x) {
    double resf = 0d;
    for (int i = 0; i < payments.length; i++) {
      resf = resf + f_xirr(payments[i], days[i], days[0], x);
    }

    return resf;
  }

  private static double total_df_xirr(double[] payments, double[] days, double x) {
    double resf = 0d;
    for (int i = 0; i < payments.length; i++) {
      resf = resf + df_xirr(payments[i], days[i], days[0], x);
    }

    return resf;
  }

  public static double Newtons_method(double guess, double[] payments, double[] days) {
    double x0 = guess;
    double x1 = 0d;
    double err = 1e+100;

    while (err > tol) {
      x1 = x0 - total_f_xirr(payments, days, x0) / total_df_xirr(payments, days, x0);
      err = Math.abs(x1 - x0);
      x0 = x1;
    }

    return x0;
  }
}
