package unbbayes.datamining.classifiers.bayesianlearning;

import unbbayes.datamining.datamanipulation.Attribute;
import java.util.*;
import java.lang.Math;

public class K2
{
    // Data
    private ParametricLearning m_pl;

    // Construtor
    public K2(ParametricLearning pl)
    {
        m_pl = pl;
    }

    // Métodos
    double gh(Attribute i, int[] pais)
    {
        int qi = m_pl.computeQ(pais);
        int ri = i.numValues();
        int[][] Nijk = m_pl.computeNijk(i.getIndex(), pais);

        int width = Nijk.length;
        int heigth = Nijk[0].length;

        int[] Nij = new int[width];
        for(int x = 0; x<width; x++)
        {
            Nij[x] = 0;
            for(int y = 0; y<heigth; y++)
            {
                Nij[x] += Nijk[x][y];
            }
        }

        float a = 0;
        for(int j = 0; j<qi; j++)
        {
            a += logfat(ri + Nij[j] - 1);
        }

        float b = 0;
        for(int j = 0; j<qi; j++)
        {
            for(int k = 0; k<ri; k++)
            {
                b += logfat(Nijk[j][k]);
            }
        }


        return qi * logfat(ri - 1) - a + b;
    }

    ///////////////////////////////////////////////////////////////////////////
    // logfat

    private double logfat(int n)
    {
        if( n <= 100)
        {
           return logfatorial(n);
        }
        else
        {
           return logstirling(n);
        }
    }

    private double logfatorial(int n)
    {
        double f = 0;

        for(int i = 1 ; i <= n ; i++)
        {
           f += Math.log(i);
        }
        return f;
    }

    private double logstirling(int n)
    {
       return (0.5*Math.log(2*Math.PI) + (n+0.5)*Math.log(n) - n*Math.log(Math.E));
    }
}
