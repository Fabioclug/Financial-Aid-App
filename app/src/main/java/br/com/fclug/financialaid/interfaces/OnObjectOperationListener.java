package br.com.fclug.financialaid.interfaces;

/**
 * Created by Fabioclug on 2017-08-30.
 *
 * Interface used to take an action on the caller Activity after dialog dismiss
 */

public interface OnObjectOperationListener {
    void onAdd();
    void onUpdate(Object object);
    void onDelete(Object object);
}
