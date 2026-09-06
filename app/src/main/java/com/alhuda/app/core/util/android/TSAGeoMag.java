package com.alhuda.app.core.util.android;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TSAGeoMag
{

    private String [] input =
	{       "    2025.0            WMM-2025     11/13/2024",
		"  1  0  -29351.8       0.0       12.0        0.0",
		"  1  1   -1410.8    4545.4        9.7      -21.5",
		"  2  0   -2556.6       0.0      -11.6        0.0",
		"  2  1    2951.1   -3133.6       -5.2      -27.7",
		"  2  2    1649.3    -815.1       -8.0      -12.1",
		"  3  0    1361.0       0.0       -1.3        0.0",
		"  3  1   -2404.1     -56.6       -4.2        4.0",
		"  3  2    1243.8     237.5        0.4       -0.3",
		"  3  3     453.6    -549.5      -15.6       -4.1",
		"  4  0     895.0       0.0       -1.6        0.0",
		"  4  1     799.5     278.6       -2.4       -1.1",
		"  4  2      55.7    -133.9       -6.0        4.1",
		"  4  3    -281.1     212.0        5.6        1.6",
		"  4  4      12.1    -375.6       -7.0       -4.4",
		"  5  0    -233.2       0.0        0.6        0.0",
		"  5  1     368.9      45.4        1.4       -0.5",
		"  5  2     187.2     220.2        0.0        2.2",
		"  5  3    -138.7    -122.9        0.6        0.4",
		"  5  4    -142.0      43.0        2.2        1.7",
		"  5  5      20.9     106.1        0.9        1.9",
		"  6  0      64.4       0.0       -0.2        0.0",
		"  6  1      63.8     -18.4       -0.4        0.3",
		"  6  2      76.9      16.8        0.9       -1.6",
		"  6  3    -115.7      48.8        1.2       -0.4",
		"  6  4     -40.9     -59.8       -0.9        0.9",
		"  6  5      14.9      10.9        0.3        0.7",
		"  6  6     -60.7      72.7        0.9        0.9",
		"  7  0      79.5       0.0       -0.0        0.0",
		"  7  1     -77.0     -48.9       -0.1        0.6",
		"  7  2      -8.8     -14.4       -0.1        0.5",
		"  7  3      59.3      -1.0        0.5       -0.8",
		"  7  4      15.8      23.4       -0.1        0.0",
		"  7  5       2.5      -7.4       -0.8       -1.0",
		"  7  6     -11.1     -25.1       -0.8        0.6",
		"  7  7      14.2      -2.3        0.8       -0.2",
		"  8  0      23.2       0.0       -0.1        0.0",
		"  8  1      10.8       7.1        0.2       -0.2",
		"  8  2     -17.5     -12.6        0.0        0.5",
		"  8  3       2.0      11.4        0.5       -0.4",
		"  8  4     -21.7      -9.7       -0.1        0.4",
		"  8  5      16.9      12.7        0.3       -0.5",
		"  8  6      15.0       0.7        0.2       -0.6",
		"  8  7     -16.8      -5.2       -0.0        0.3",
		"  8  8       0.9       3.9        0.2        0.2",
		"  9  0       4.6       0.0       -0.0        0.0",
		"  9  1       7.8     -24.8       -0.1       -0.3",
		"  9  2       3.0      12.2        0.1        0.3",
		"  9  3      -0.2       8.3        0.3       -0.3",
		"  9  4      -2.5      -3.3       -0.3        0.3",
		"  9  5     -13.1      -5.2        0.0        0.2",
		"  9  6       2.4       7.2        0.3       -0.1",
		"  9  7       8.6      -0.6       -0.1       -0.2",
		"  9  8      -8.7       0.8        0.1        0.4",
		"  9  9     -12.9      10.0       -0.1        0.1",
		" 10  0      -1.3       0.0        0.1        0.0",
		" 10  1      -6.4       3.3        0.0        0.0",
		" 10  2       0.2       0.0        0.1       -0.0",
		" 10  3       2.0       2.4        0.1       -0.2",
		" 10  4      -1.0       5.3       -0.0        0.1",
		" 10  5      -0.6      -9.1       -0.3       -0.1",
		" 10  6      -0.9       0.4        0.0        0.1",
		" 10  7       1.5      -4.2       -0.1        0.0",
		" 10  8       0.9      -3.8       -0.1       -0.1",
		" 10  9      -2.7       0.9       -0.0        0.2",
		" 10 10      -3.9      -9.1       -0.0       -0.0",
		" 11  0       2.9       0.0        0.0        0.0",
		" 11  1      -1.5       0.0       -0.0       -0.0",
		" 11  2      -2.5       2.9        0.0        0.1",
		" 11  3       2.4      -0.6        0.0       -0.0",
		" 11  4      -0.6       0.2        0.0        0.1",
		" 11  5      -0.1       0.5       -0.1       -0.0",
		" 11  6      -0.6      -0.3        0.0       -0.0",
		" 11  7      -0.1      -1.2       -0.0        0.1",
		" 11  8       1.1      -1.7       -0.1       -0.0",
		" 11  9      -1.0      -2.9       -0.1        0.0",
		" 11 10      -0.2      -1.8       -0.1        0.0",
		" 11 11       2.6      -2.3       -0.1        0.0",
		" 12  0      -2.0       0.0        0.0        0.0",
		" 12  1      -0.2      -1.3        0.0       -0.0",
		" 12  2       0.3       0.7       -0.0        0.0",
		" 12  3       1.2       1.0       -0.0       -0.1",
		" 12  4      -1.3      -1.4       -0.0        0.1",
		" 12  5       0.6      -0.0       -0.0       -0.0",
		" 12  6       0.6       0.6        0.1       -0.0",
		" 12  7       0.5      -0.1       -0.0       -0.0",
		" 12  8      -0.1       0.8        0.0        0.0",
		" 12  9      -0.4       0.1        0.0       -0.0",
		" 12 10      -0.2      -1.0       -0.1       -0.0",
		" 12 11      -1.3       0.1       -0.0        0.0",
		" 12 12      -0.7       0.2       -0.1       -0.1"
	};

    private double alt = 0;

    private double glat = 0;

    private double glon = 0;

    private double time = 0;

    private double dec = 0;

    private double dip = 0;

    private double ti = 0;

    private int maxdeg = 12;

    private int maxord;

    private double defaultDate = 2022.5;

    private final double defaultAltitude = 0;

    private double c[][] = new double[13][13];

    private double cd[][] = new double[13][13];

    private double tc[][] = new double[13][13];

    private double dp[][] = new double[13][13];

    private double snorm[] = new double[169];

    private double sp[] = new double[13];

    private double cp[] = new double[13];
    private double fn[] = new double[13];
    private double fm[] = new double[13];

    private double pp[] = new double[13];

    private double k[][] = new double[13][13];

    private double otime, oalt, olat, olon;

    private double epoch;

    private double bx,by,bz,bh;

    private double re,a2,b2,c2,a4,b4,c4;

    private double r,d,ca,sa,ct,st;

    public TSAGeoMag()
    {

	initModel();
    }

    private void initModel()
    {
	glat = 0;
	glon = 0;

	maxord = maxdeg;
	sp[0] = 0.0;
	cp[0] = snorm[0] = pp[0] = 1.0;
	dp[0][0] = 0.0;

	double a = 6378.137;

	double b = 6356.7523142;

	re = 6371.2;
	a2 = a * a;
	b2 = b * b;
	c2 = a2 - b2;
	a4 = a2 * a2;
	b4 = b2 * b2;
	c4 = a4 - b4;

	try{

	    Reader is;

	    InputStream input = getClass().getResourceAsStream("WMM.COF");
	    if(input == null) throw new FileNotFoundException("WMM.COF not found");
	    is = new InputStreamReader(input);
	    StreamTokenizer str = new StreamTokenizer(is);

	    c[0][0] = 0.0;
	    cd[0][0] = 0.0;
	    str.nextToken();
	    epoch = str.nval;
	    defaultDate = epoch + 2.5;
	    str.nextToken();

	    str.nextToken();

	    while(true){
		str.nextToken();
		if(str.nval >= 9999)
		    break;

		int n = (int)str.nval;
		str.nextToken();
		int m = (int)str.nval;
		str.nextToken();
		double gnm = str.nval;
		str.nextToken();
		double hnm = str.nval;
		str.nextToken();
		double dgnm = str.nval;
		str.nextToken();
		double dhnm = str.nval;

		if (m <= n)
		{
		    c[m][n] = gnm;
		    cd[m][n] = dgnm;

		    if (m != 0)
		    {
			c[n][m-1] = hnm;
			cd[n][m-1] = dhnm;
		    }
		}

	    }

	    is.close();
	}

	catch(FileNotFoundException e)
	{
	    String msg = "\nNOTICE      NOTICE      NOTICE      \n" +
		    "WMMCOF file not found in TSAGeoMag.InitModel()\n" +
		    "The input file WMM.COF was not found in the same\n" +
		    "directory as the application.\n" +
		    "The magnetic field components are set to internal values.\n";
	    System.out.println(msg);
	    e.printStackTrace();

	    setCoeff();
	}

	catch(IOException e)
	{
	    String msg = "\nNOTICE      NOTICE      NOTICE      \n" +
		    "Problem reading the WMMCOF file in TSAGeoMag.InitModel()\n" +
		    "The input file WMM.COF was found, but there was a problem \n" +
		    "reading the data.\n" +
		    "The magnetic field components are set to internal values.";

	    System.out.println(msg);
	    e.printStackTrace();

	    setCoeff();
	}

	snorm[0] = 1.0;
	for (int n = 1; n <= maxord; n++){

	    snorm[n] = snorm[n - 1] * (2 * n - 1) / n;
	    int j = 2;

	    for(int m = 0,D1 = 1,D2 = (n - m + D1) / D1; D2 > 0; D2--, m += D1){
		k[m][n] = (double) (((n - 1) * (n - 1))-(m * m))/(double) ((2 * n-1)*(2*n-3));
		if(m > 0){
		    double flnmj = ((n - m + 1) * j) / (double) (n + m);
		    snorm[n + m * 13] = snorm[n + (m -1 ) * 13] * Math.sqrt(flnmj);
		    j = 1;
		    c[n][m-1] = snorm[n + m * 13] * c[n][m-1];
		    cd[n][m-1] = snorm[n + m * 13] * cd[n][m-1];
		}
		c[m][n] = snorm[n + m * 13] * c[m][n];
		cd[m][n] = snorm[n + m * 13] * cd[m][n];
	    }

	    fn[n] = (n+1);
	    fm[n] = n;

	}

	k[1][1] = 0.0;

	otime = oalt = olat = olon = -1000.0;

    }

    private void calcGeoMag(double fLat, double fLon, double year, double altitude)
    {

	glat =  fLat;
	glon =  fLon;
	alt = altitude;

	double validEnd = epoch + 5.0;
	time = Math.max(epoch, Math.min(year, validEnd));

	double dt = time - epoch;

	double pi = Math.PI;
	double dtr = (pi/180.0);
	double rlon = glon * dtr;
	double rlat = glat * dtr;
	double srlon = Math.sin(rlon);
	double srlat = Math.sin(rlat);
	double crlon = Math.cos(rlon);
	double crlat = Math.cos(rlat);
	double srlat2 = srlat * srlat;
	double crlat2 = crlat * crlat;
	sp[1] = srlon;
	cp[1] = crlon;

	if (alt != oalt || glat != olat){
	    double q = Math.sqrt(a2 - c2 * srlat2);
	    double q1 = alt * q;
	    double q2 = ((q1 + a2) / (q1 + b2)) * ((q1 + a2) / (q1 + b2));
	    ct = srlat / Math.sqrt(q2 * crlat2 + srlat2);
	    st = Math.sqrt(1.0 - (ct * ct));
	    double r2 = ((alt*alt) + 2.0 * q1 + (a4 - c4 * srlat2) / (q * q));
	    r = Math.sqrt(r2);
	    d = Math.sqrt(a2 * crlat2 + b2 * srlat2);
	    ca = (alt + d) / r;
	    sa = c2 * crlat * srlat / (r * d);
	}
	if (glon != olon){
	    for (int m = 2; m <= maxord; m++){
		sp[m] = sp[1] * cp[m-1] + cp[1] * sp[m-1];
		cp[m] = cp[1] * cp[m-1] - sp[1] * sp[m-1];
	    }
	}
	double aor = re / r;
	double ar = aor * aor;
	double br = 0, bt = 0, bp = 0, bpp = 0;

	for(int n = 1; n <= maxord; n++){
	    ar = ar * aor;
	    for (int m = 0,D3 = 1,D4 = (n + m + D3) / D3; D4 > 0; D4--,m += D3){

		if(alt != oalt || glat != olat){
		    if(n == m){
			snorm[n + m * 13] = st * snorm[n - 1 + (m - 1) * 13];
			dp[m][n] = st * dp[m-1][n-1]+ ct* snorm[n - 1 + (m - 1) * 13];
		    }
		    if(n == 1 && m == 0){
			snorm[n + m * 13] = ct * snorm[n - 1 + m * 13];
			dp[m][n] = ct * dp[m][n - 1] - st * snorm[n - 1 + m * 13];
		    }
		    if(n > 1 && n != m){
			if(m > n - 2)
			    snorm[n - 2 + m * 13] = 0.0;
			if(m > n - 2)
			    dp[m][n - 2] = 0.0;
			snorm[n + m * 13] = ct * snorm[n - 1 + m * 13] - k[m][n] * snorm[n - 2 + m * 13];
			dp[m][n] = ct * dp[m][n - 1] - st * snorm[n - 1 + m * 13] - k[m][n] * dp[m][n - 2];
		    }
		}

		if(time != otime){
		    tc[m][n] = c[m][n] + dt * cd[m][n];

		    if(m != 0)
			tc[n][m - 1] = c[n][m - 1]+ dt * cd[n][m - 1];
		}

		double temp1, temp2;
		double par = ar * snorm[ n + m * 13];
		if(m == 0){
		    temp1 = tc[m][n] * cp[m];
		    temp2 = tc[m][n] * sp[m];
		}
		else{
		    temp1 = tc[m][n] * cp[m] + tc[n][m - 1] * sp[m];
		    temp2 = tc[m][n] * sp[m] - tc[n][m - 1] * cp[m];
		}

		bt = bt - ar * temp1 * dp[m][n];
		bp += (fm[m] * temp2 * par);
		br += (fn[n] * temp1 * par);

		if(st == 0.0 && m == 1){
		    if(n == 1)
			pp[n] = pp[n - 1];
		    else
			pp[n] = ct * pp[n - 1] - k[m][n] * pp[n - 2];
		    double parp = ar * pp[n];
		    bpp += (fm[m] * temp2 * parp);
		}

	    }

	}

	if(st == 0.0)
	    bp = bpp;
	else
	    bp /= st;

	bx = -bt * ca - br * sa;
	by = bp;
	bz = bt * sa - br * ca;

	bh = Math.sqrt((bx * bx)+(by * by));
	ti = Math.sqrt((bh * bh)+(bz * bz));

	dec = (Math.atan2(by, bx) / dtr);
	dip = (Math.atan2(bz, bh) / dtr);

	otime = time;
	oalt = alt;
	olat = glat;
	olon = glon;

    }

    public double getDeclination( double dlat, double dlong )
    {
	calcGeoMag( dlat, dlong, defaultDate, defaultAltitude );
	return  dec;
    }

    public double getDeclination( double dlat, double dlong, double year, double altitude )
    {
	calcGeoMag( dlat, dlong, year, altitude );
	return  dec;
    }

    public double getIntensity( double dlat, double dlong )
    {
	calcGeoMag( dlat, dlong, defaultDate, defaultAltitude );
	return  ti;
    }

    public double getIntensity( double dlat, double dlong, double year, double altitude )
    {
	calcGeoMag( dlat, dlong, year, altitude );
	return  ti;
    }

    public double getHorizontalIntensity( double dlat, double dlong )
    {
	calcGeoMag( dlat, dlong, defaultDate, defaultAltitude );
	return  bh;
    }

    public double getHorizontalIntensity( double dlat, double dlong, double year, double altitude )
    {
	calcGeoMag( dlat, dlong, year, altitude );
	return  bh;
    }

    public double getVerticalIntensity( double dlat, double dlong )
    {
	calcGeoMag( dlat, dlong, defaultDate, defaultAltitude );
	return  bz;
    }

    public double getVerticalIntensity( double dlat, double dlong, double year, double altitude )
    {
	calcGeoMag( dlat, dlong, year, altitude );
	return  bz;
    }

    public double getNorthIntensity( double dlat, double dlong )
    {
	calcGeoMag( dlat, dlong, defaultDate, defaultAltitude );
	return  bx;
    }

    public double getNorthIntensity( double dlat, double dlong, double year, double altitude )
    {
	calcGeoMag( dlat, dlong, year, altitude );
	return  bx;
    }

    public double getEastIntensity( double dlat, double dlong )
    {
	calcGeoMag( dlat, dlong, defaultDate, defaultAltitude );
	return  by;
    }

    public double getEastIntensity( double dlat, double dlong, double year, double altitude )
    {
	calcGeoMag( dlat, dlong, year, altitude );
	return  by;
    }

    public double getDipAngle( double dlat, double dlong )
    {
	calcGeoMag( dlat, dlong, defaultDate, defaultAltitude );
	return  dip;
    }

    public double getDipAngle( double dlat, double dlong, double year, double altitude )
    {
	calcGeoMag( dlat, dlong, year, altitude );
	return  dip;
    }

    private void setCoeff()
    {
	c[0][0] = 0.0;
	cd[0][0] = 0.0;

	epoch = Double.parseDouble(input[0].trim().split("[\\s]+")[0]);
	defaultDate = epoch + 2.5;

	String [] tokens;

	for(int i=1; i<input.length; i++)
	{
	    tokens = input[i].trim().split("[\\s]+");

	    int n = Integer.parseInt(tokens[0]);
	    int m = Integer.parseInt(tokens[1]);
	    double gnm = Double.parseDouble(tokens[2]);
	    double hnm = Double.parseDouble(tokens[3]);
	    double dgnm = Double.parseDouble(tokens[4]);
	    double dhnm = Double.parseDouble(tokens[5]);

	    if (m <= n)
	    {
		c[m][n] = gnm;
		cd[m][n] = dgnm;

		if (m != 0)
		{
		    c[n][m-1] = hnm;
		    cd[n][m-1] = dhnm;
		}
	    }
	}
    }

    public double decimalYear(GregorianCalendar cal)
    {
	int year = cal.get(Calendar.YEAR);
	double daysInYear;
	if(cal.isLeapYear(year))
	{
	    daysInYear = 366.0;
	}
	else
	{
	    daysInYear = 365.0;
	}

	return year + (cal.get(Calendar.DAY_OF_YEAR))/daysInYear;
    }
}
