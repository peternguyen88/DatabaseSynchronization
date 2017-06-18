package com.zeth;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "mt_question")
public class Question {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    long id;

    @Column
    String pack_no;

    @Column
    String set_no;

    @Column
    String question_no;

    @Column
    String question_type;

    @Column
    String reading_passage;

    @Column
    String stimulus;

    @Column
    String option_A;

    @Column
    String option_B;

    @Column
    String option_C;

    @Column
    String option_D;

    @Column
    String option_E;

    @Column
    String correct_answer;

    @Column
    String photo_url;

    @ManyToOne
    @JoinColumn(name = "set_no", insertable = false, updatable = false)
    private Question_set set;

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof Question)) return false;
        Question other = (Question) obj;
        return Objects.equals(this.pack_no, other.pack_no) && Objects.equals(this.set_no, other.set_no) && Objects.equals(this.question_no, other.question_no);
    }
}
