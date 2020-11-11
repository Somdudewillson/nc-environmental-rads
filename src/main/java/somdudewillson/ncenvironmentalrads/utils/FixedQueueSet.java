package somdudewillson.ncenvironmentalrads.utils;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class FixedQueueSet<T> extends AbstractSet<T> {
	private final HashSet<T> elements;
	private final ArrayList<T> ordered;
	
	private final int capacity;
	
	public FixedQueueSet(int capacity) {
		this.capacity = capacity;
		int trueCapacity = (int) Math.ceil(capacity/0.75f);
		
		elements = new HashSet<T>(trueCapacity);
		ordered = new ArrayList<T>(trueCapacity);
	}

	@Override
	public boolean add(T arg0) {
		if (ordered.size() >= capacity) {
			elements.remove(ordered.get(0));
			ordered.remove(0);
		}
		
		ordered.add(arg0);
		return elements.add(arg0);
	}

	@Override
	public boolean contains(Object o) {
		return elements.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return ordered.iterator();
	}

	@Override
	public int size() {
		return elements.size();
	}

}
