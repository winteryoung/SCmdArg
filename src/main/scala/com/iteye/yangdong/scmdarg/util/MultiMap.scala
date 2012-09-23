package com.iteye.yangdong.scmdarg.util

import collection.mutable
import collection.mutable.ArrayBuffer

/**
 * @author Winter Young
 */
trait MultiMap[A, B] extends mutable.Map[A, ArrayBuffer[B]] {
  /** Creates a new set.
   *
   *  Classes that use this trait as a mixin can override this method
   *  to have the desired implementation of sets assigned to new keys.
   *  By default this is `HashSet`.
   *
   *  @return An empty set of values of type `B`.
   */
  protected def makeSet: ArrayBuffer[B] = new ArrayBuffer[B]

  /** Assigns the specified `value` to a specified `key`, replacing
   *  the existing value assigned to that `key` if it is equal to
   *  the specified value. Otherwise, simply adds another binding to
   *  the `key`.
   *
   *  @param key    The key to which to bind the new value.
   *  @param value  The value to bind to the key.
   *  @return       A reference to this multimap.
   */
  def addBinding(key: A, value: B): this.type = {
    get(key) match {
      case None =>
        val set = makeSet
        set += value
        this(key) = set
      case Some(set) =>
        set += value
    }
    this
  }

  /** Removes the binding of `value` to `key` if it exists.
   *
   *  If this was the last value assigned to the specified key, the
   *  set assigned to that key will be removed as well.
   *
   *  @param key     The key of the binding.
   *  @param value   The value to remove.
   *  @return        A reference to this multimap.
   */
  def removeBinding(key: A, value: B): this.type = {
    get(key) match {
      case None =>
      case Some(set) =>
        set -= value
        if (set.isEmpty) this -= key
    }
    this
  }

  /** Checks if there exists a binding to `key` such that it satisfies the predicate `p`.
   *
   *  @param key   The key for which the predicate is checked.
   *  @param p     The predicate which a value assigned to the key must satisfy.
   *  @return      A boolean if such a binding exists
   */
  def entryExists(key: A, p: B => Boolean): Boolean = get(key) match {
    case None => false
    case Some(set) => set exists p
  }
}