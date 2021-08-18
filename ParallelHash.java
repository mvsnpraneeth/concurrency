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

class ParallelHash
{
    ArrayList<Pair<Object,Object>> buck = new ArrayList<>();
    int hashSize = 2,curSize=0;

    ParallelHash(int hsize)
    {
        hashSize = hsize;
        for(int i=1;i<=hashSize;i++)
            buck.add(null);
    }
    public void doubleit()
    {
        for (int i = 1; i <= hashSize; i++)
            buck.add(null);
        hashSize *= 2;
    }

    public void put(Object k,Object v)
    {
        Pair<Object,Object> t = new Pair<>(k,v);
        int hval = (k.hashCode()) % hashSize;
        if(buck.get(hval)==null)
        {
            synchronized (this)
            {
                buck.set(hval, t);
                curSize++;
                 if(curSize==hashSize)
                     doubleit();
            }
        }
        else
        {
            boolean fnd = false;
            for(int i=hval+1; i<hashSize; i++)
            {
                if(buck.get(i)==null)
                {
                    synchronized (this)
                    {
                        buck.set(i, t);
                        fnd = true;
                        curSize++;
                        if(curSize==hashSize)
                            doubleit();
                    }

                    break;
                }
            }
            if(!fnd)
            {
                for(int i=0; i<hval; i++)
                {
                    if(buck.get(i)==null)
                    {
                        synchronized (this)
                        {
                            buck.set(i,t);
                            fnd = true;
                            curSize++;
                            if(curSize==hashSize)
                                doubleit();
                        }
                        break;
                    }
                }
                if(!fnd)
                {
                    put(k,v);
                }
            }
        }
    }

    public Object get(Object k)
    {
        int hval = (k.hashCode()) % hashSize ;
        if(buck.get(hval)!=null)
        {
            if(buck.get(hval).getKey().equals(k))
            {
                synchronized (this)
                {
                    return  buck.get(hval).getVal();
                }
            }
        }
        else
        {
             for(int i=hval+1; i<hashSize; i++)
             {
                 if(buck.get(i)!=null)
                 {
                      if(buck.get(i).getKey().equals(k))
                      {
                          synchronized (this)
                          {
                              return  buck.get(i).getVal();
                          }
                      }

                 }
             }
             for(int i=0; i<hval; i++)
             {
                  if(buck.get(i)!=null)
                  {
                       if(buck.get(i).getKey().equals(k))
                       {
                           synchronized (this)
                           {
                               return  buck.get(i).getVal();
                           }
                       }

                  }

             }
        }
        return null;
    }

    public boolean remove(Object k)
    {
        int hval = (k.hashCode()) % hashSize ;
        if(buck.get(hval)!=null)
        {
            if (buck.get(hval).getKey().equals(k)) {
                synchronized (this) {
                    if (buck.get(hval) != null) {
                        buck.set(hval, null);
                        curSize--;
                        return true;
                    }
                }

            }
        }
        else
        {
                     for(int i=hval+1; i<hashSize; i++)
                     {
                         if(buck.get(i)!=null)
                         {
                              if(buck.get(i).getKey().equals(k))
                              {
                                  synchronized (this)
                                  {
                                      if (buck.get(i) != null) {
                                          buck.set(i, null);
                                          curSize--;
                                          return true;
                                      }
                                  }
                              }

                         }
                     }
                     for(int i=0; i<hval; i++)
                     {
                          if(buck.get(i)!=null)
                          {
                               if(buck.get(i).getKey().equals(k))
                               {
                                   synchronized (this)
                                   {
                                       if (buck.get(i) != null) {
                                           buck.set(i, null);
                                           curSize--;
                                           return true;
                                       }
                                   }
                               }

                          }

                     }
        }

        return false;
    }

    public int getSize()
    {
        return curSize;
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
        while(t1.isAlive() || t2.isAlive()){}

        System.out.println("size is "+h.getSize());
        //System.out.println("hashsize is "+h.hashSize);
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