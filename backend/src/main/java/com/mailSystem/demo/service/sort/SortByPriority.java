package com.mailSystem.demo.service.sort;

import com.mailSystem.demo.model.Mail;

import java.util.Comparator;
import java.util.List;

public class SortByPriority implements ISortStrategy {

    private final boolean ascending;

    // كونسرتكتور بياخد القيمة (عشان المصنع يعرف يبعت true/false)
    public SortByPriority(boolean ascending) {
        this.ascending = ascending;
    }

    // كونستركتور فاضي (Default)
    public SortByPriority() {
        this(false);
    }

    @Override
    public List<Mail> sort(List<Mail> emails) {
        // 1. مقارنة الأولوية (Priority)
        // لو null بنعتبره 1 (أقل أولوية) عشان ميضربش
        Comparator<Mail> priorityComparator = Comparator.comparing(
                (Mail m) -> m.getPriority() == null ? 1 : m.getPriority()
        );

        // لو الترتيب تنازلي (من المهم للأقل أهمية)، نعكس المقارنة
        if (!ascending) {
            priorityComparator = priorityComparator.reversed();
        }

        // 2. كاسر التعادل (Tie-Breaker): التاريخ
        // بنقوله: لو الأولويات زي بعض، رتبهم بالتاريخ
        // أهم حتة: nullsLast عشان لو مفيش تاريخ يرميه في الآخر وميضربش Error
        priorityComparator = priorityComparator.thenComparing(
                Comparator.comparing(
                        Mail::getTimestamp,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed() // دايماً الأحدث فوق في حالة تساوي الأولوية
        );

        // 3. التنفيذ
        try {
            emails.sort(priorityComparator);
            System.out.println("✅ SortByPriority completed successfully for " + emails.size() + " emails.");
        } catch (Exception e) {
            System.err.println("🔥 Critical Error inside SortByPriority: " + e.getMessage());
            e.printStackTrace();
        }

        return emails;
    }
}