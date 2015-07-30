package io.scif;

/**
 * @author hinerm
 *
 * @param <T> - The backing type (e.g. byte[], bufferedimage, etc..)
 */
public interface TypedBlock<T> extends Block {

	T get();
}
