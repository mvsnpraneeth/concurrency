import java.io.*;
import java.util.*;

class Pair<T1,T2>
{
    T1 key;
    T2 val;
    Pair(T1 k,T2 v)
    {
        key=k;
        val=v;
    }
    T1 getKey(){return key;}
    T2 getVal(){return val;}
}

class ParallelHash<T1,T2>
{
    List<LinkedList<Pair<T1,T2>>> buck = new ArrayList<LinkedList<Pair<T1,T2>>>();
    int hashSize = 2,curSize=0;
    Object[] locks = new Object[hashSize];

    ParallelHash(int hsize)
    {
        hashSize = hsize;
        for(int i=0;i<hashSize;i++)
        {
            locks[i] = new Object();
            buck.add(null);
        }
    }

    public void doubleit()
    {
        for (int i = 1; i <= hashSize; i++)
            buck.add(null);
        hashSize *= 2;
        locks = new Object[hashSize];
    }

    public void put(T1 k,T2 v)
    {
        int hval = (k.hashCode()) % hashSize;
        if(buck.get(hval)==null)
        {
            synchronized (locks[hval])
            {
                LinkedList<Pair<T1,T2>> t = new LinkedList<Pair<T1,T2>>();
                t.add(new Pair<T1,T2>(k,v));
                buck.set(hval, t);
            }
            synchronized (this)
            {
                curSize++;
            }
        }
        else
        {
            synchronized (locks[hval])
            {
                buck.get(hval).add(new Pair<T1,T2>(k,v));
            }
            synchronized (this)
            {
                curSize++;
            }
        }
    }

    public Object get(T1 k)
    {
        int hval = (k.hashCode()) % hashSize ;
        if(buck.get(hval)!=null)
        {

            synchronized (locks[hval])
            {
                for(Pair<T1,T2> p:buck.get(hval))
                {
                    if(p.getKey().equals(k))
                        return p.getVal();
                }
            }
        }
        return null;
    }

    public boolean remove(T1 k)
    {
        boolean fnd=false;
        int hval = (k.hashCode()) % hashSize;
        if(buck.get(hval)!=null)
        {
            synchronized (locks[hval])
            {
                for(int i=0;i<buck.get(hval).size();i++)
                {
                    if(buck.get(hval).get(i).getKey().equals(k))
                    {
                        fnd=true;
                        buck.get(hval).remove(i);
                        break;
                    }
                }

            }
        }
        if(fnd)
        {
            synchronized (this){
                curSize--;
            }
        }
        return fnd;
    }


    public int getSize()
    {
        synchronized(this)
        {
            return curSize;
        }

    }

    public static void main(String[] arg)
    {
        ParallelHash h = new ParallelHash(2);
        /*h.put("a",1);
        h.put("b",2);
        h.put("c",3);

        System.out.println("value is "+h.get("b"));
        boolean check  = h.remove("b");
        if(check)
            System.out.println("REMOVED ");
        System.out.println("value is "+h.getSize());*/
        Multi m = new Multi(h);
        Thread t1 =new Thread(m);
        Thread t2 =new Thread(m);
        t1.start();t2.start();
        try{

            t1.join();t2.join();
        }
        catch(Exception ex)
        {
            System.out.println("Join error");
        }
//        while(t1.isAlive() || t2.isAlive()){}
        System.out.println("size is "+h.getSize());
    }
}
class Multi implements Runnable
{
    ParallelHash p;
    Multi(ParallelHash a)
    {
        p=a;
    }
    public void run() {
        p.put("p",1);
        p.put("e",3);
        p.put("c",5);
        p.remove("c");
    }
}