import java.util.*;

public class MaekawaMutex extends Process implements Lock
{
	//Kvorum
	int[] kvorum;
	int K, k;
	
	//varijable
	boolean taken;
	//kada je taken == true, ne smijemo slati reply
	
	//Sat
	int C;
	
	Queue<Integer> pending;
	
	
	
	public MaekawaMutex(Linker initComm)
	{
		super(initComm);
		
		taken = false;
		
		C=0;
		
		pending = new LinkedList<>();
		
		System.out.println("Definiraj kvorum za " + myId);
		
		Scanner in = new Scanner(System.in);
		K=in.nextInt();
		
		kvorum = new int[K];
		
		for(int i=0; i<K; i++)
		{
			kvorum[i]=in.nextInt();
		}
	}
	
	public synchronized void requestCS()
	{
		C=C+1;
		
		k=K;
		for(int i=0; i<K; i++)
		{
			sendMsg(kvorum[i], "request", C);
		}
		
		//cekamo odgovore
		while(k>0)
		{
			myWait();
		}
		
	}
	
	public synchronized void releaseCS()
	{
		for(int i=0; i<K; i++)
		{
			sendMsg(kvorum[i], "release", C);
		}
		
	}
	
	public synchronized void handleMsg(Msg m, int src, String tag)
	{
		int Ck=m.getMessageInt();
		C=Math.max(C, Ck);
		
		if(tag.equals("request"))
		{
			
			//dva slucaja
			//prvi
			if(taken)
			{
				//dodajemo posiljatelja u red cekanja
				pending.add(src);
			}
			//drugi slucaj
			else
			{
				sendMsg(src, "reply", C);
				taken=true;
			}
		}
		
		if(tag.equals("reply"))
		{
			k--;
			notify();
		}
		
		if(tag.equals("release"))
		{
			if(pending.size()>0)
			{
				sendMsg(pending.element(), "reply", C);
				pending.remove();
			}
			else
			{
				taken=false;
			}
		}
	}
}