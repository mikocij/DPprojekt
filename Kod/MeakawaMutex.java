import java.util.*;

public class MeakawaMutex extends Process implements Lock
{
	//Kvorum
	int[] kvorum;
	int K, k;
	
	//varijable
	boolean taken;
	//kada je taken == true, ne smijemo slati reply
	
	//Sat
	int C;
	
	Queue<Ineger> pending;
	
	
	
	public MeakawaMutex(Linker initComm)
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
	
	public synchronised void requestCS()
	{
		C=C+1;
		
		k=K;
		for(int i=0; i<K; i++)
		{
			sendMsg(i, "request", C);
		}
		
		//cekamo odgovore
		while(k>0)
		{
			myWait();
		}
		
	}
	
	public synchronised void releaseCS()
	{
		for(int i=0; i<K; i++)
		{
			sendMsg(i, "release", C);
		}
		
	}
	
	public synchronised void handleMsg(Msg m, int src, String tag)
	{
		int Ck=m.getMessageInt();
		C=max(C, Ck);
		
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
			}
		}
		
		if(tag.equals("reply"))
		{
			k--;
			//?notify();
		}
		
		if(tag.equals("release"))
		{
			if(pending.size()>0)
			{
				sendMsg(pending.element(), "reply", C);
				pending.remove();
			}
		}
	}
}