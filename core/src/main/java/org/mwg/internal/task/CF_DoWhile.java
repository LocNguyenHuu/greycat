package org.mwg.internal.task;

import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.plugin.SchedulerAffinity;
import org.mwg.task.*;

import java.util.Map;

class CF_DoWhile extends CF_Action {

    private final ConditionalFunction _cond;
    private final Task _then;
    private final String _conditionalScript;

    CF_DoWhile(final Task p_then, final ConditionalFunction p_cond, final String conditionalScript) {
        super();
        this._cond = p_cond;
        this._then = p_then;
        this._conditionalScript = conditionalScript;
    }

    @Override
    public void eval(final TaskContext ctx) {
        final CoreTaskContext coreTaskContext = (CoreTaskContext) ctx;
        final CF_DoWhile selfPointer = this;
        final Callback[] recursiveAction = new Callback[1];
        recursiveAction[0] = new Callback<TaskResult>() {
            @Override
            public void on(final TaskResult res) {
                final TaskResult previous = coreTaskContext._result;
                coreTaskContext._result = res;
                Exception exceptionDuringTask = null;
                if (res != null) {
                    if (res.output() != null) {
                        ctx.append(res.output());
                    }
                    if (res.exception() != null) {
                        exceptionDuringTask = res.exception();
                    }
                }
                if (_cond.eval(ctx) && exceptionDuringTask == null) {
                    if (previous != null) {
                        previous.free();
                    }
                    selfPointer._then.executeFrom(ctx, coreTaskContext._result, SchedulerAffinity.SAME_THREAD, recursiveAction[0]);
                } else {
                    if (previous != null) {
                        previous.free();
                    }
                    if (exceptionDuringTask != null) {
                        ctx.endTask(res, exceptionDuringTask);
                    } else {
                        ctx.continueWith(res);
                    }
                }
            }
        };
        _then.executeFrom(ctx, coreTaskContext._result, SchedulerAffinity.SAME_THREAD, recursiveAction[0]);
    }

    @Override
    public Task[] children() {
        Task[] children_tasks = new Task[1];
        children_tasks[0] = _then;
        return children_tasks;
    }

    @Override
    public void cf_serialize(StringBuilder builder, Map<Integer, Integer> dagIDS) {
        if (_conditionalScript == null) {
            throw new RuntimeException("Closure is not serializable, please use Script version instead!");
        }
        builder.append(CoreActionNames.DO_WHILE);
        builder.append(Constants.TASK_PARAM_OPEN);
        final CoreTask castedAction = (CoreTask) _then;
        final int castedActionHash = castedAction.hashCode();
        if (dagIDS == null || !dagIDS.containsKey(castedActionHash)) {
            builder.append(Constants.SUB_TASK_OPEN);
            castedAction.serialize(builder, dagIDS);
            builder.append(Constants.SUB_TASK_CLOSE);
        } else {
            builder.append("" + dagIDS.get(castedActionHash));
        }
        builder.append(Constants.TASK_PARAM_SEP);
        TaskHelper.serializeString(_conditionalScript, builder, true);
        builder.append(Constants.TASK_PARAM_CLOSE);
    }

}
