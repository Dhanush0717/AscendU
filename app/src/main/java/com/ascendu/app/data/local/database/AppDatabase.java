package com.ascendu.app.data.local.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.ascendu.app.data.local.dao.DailyRoutineDao;
import com.ascendu.app.data.local.dao.QuestionDao;
import com.ascendu.app.data.local.dao.QuoteDao;
import com.ascendu.app.data.local.dao.RecurringTaskDao;
import com.ascendu.app.data.local.dao.RecurringTaskOccurrenceDao;
import com.ascendu.app.data.local.dao.SkillDao;
import com.ascendu.app.data.local.dao.StudySessionDao;
import com.ascendu.app.data.local.dao.TaskDao;
import com.ascendu.app.data.local.dao.UserProfileDao;
import com.ascendu.app.data.local.entity.DailyRoutine;
import com.ascendu.app.data.local.entity.DailyRoutineCompletion;
import com.ascendu.app.data.local.entity.Question;
import com.ascendu.app.data.local.entity.Quote;
import com.ascendu.app.data.local.entity.RecurringTask;
import com.ascendu.app.data.local.entity.RecurringTaskOccurrence;
import com.ascendu.app.data.local.entity.Skill;
import com.ascendu.app.data.local.entity.StudySession;
import com.ascendu.app.data.local.entity.Task;
import com.ascendu.app.data.local.entity.UserProfile;
import com.ascendu.app.util.AppExecutors;

import java.util.ArrayList;
import java.util.List;

/**
 * Authoritative local Room Database for AscendU.
 * MCA Syllabus Compliance: SQLite Database, DAOs, Threading & Persistence.
 */
@Database(
        entities = {
                UserProfile.class,
                RecurringTask.class,
                RecurringTaskOccurrence.class,
                Task.class,
                Quote.class,
                // Legacy entities preserved for graceful migration / existing screens
                Skill.class,
                StudySession.class,
                Question.class,
                DailyRoutine.class,
                DailyRoutineCompletion.class
        },
        version = 3,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "ascendu_db";
    private static volatile AppDatabase sInstance;

    // Primary DAOs for New Planner Architecture
    public abstract UserProfileDao userProfileDao();
    public abstract RecurringTaskDao recurringTaskDao();
    public abstract RecurringTaskOccurrenceDao recurringTaskOccurrenceDao();
    public abstract TaskDao taskDao();
    public abstract QuoteDao quoteDao();

    // Legacy DAOs (retained during transitional milestones)
    public abstract SkillDao skillDao();
    public abstract StudySessionDao studySessionDao();
    public abstract QuestionDao questionDao();
    public abstract DailyRoutineDao dailyRoutineDao();

    public static AppDatabase getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .fallbackToDestructiveMigration()
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // Seed initial independent motivational quote catalog
                                    AppExecutors.getInstance().diskIO().execute(() -> {
                                        seedQuotes(getInstance(context));
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return sInstance;
    }

    /**
     * Independent seeding of universal discipline and consistency quotes.
     * Quotes are independent of user onboarding and require no user tasks.
     */
    public static void seedQuotes(AppDatabase database) {
        if (database.quoteDao().getQuoteCountDirect() > 0) {
            return;
        }

        List<Quote> initialQuotes = new ArrayList<>();
        initialQuotes.add(new Quote("We are what we repeatedly do. Excellence, then, is not an act, but a habit.", "Will Durant (on Aristotle)", false, ""));
        initialQuotes.add(new Quote("Discipline equals freedom.", "Jocko Willink", false, ""));
        initialQuotes.add(new Quote("You do not rise to the level of your goals. You fall to the level of your systems.", "James Clear", false, ""));
        initialQuotes.add(new Quote("Small disciplines repeated with consistency every day lead to great achievements gained slowly over time.", "John C. Maxwell", false, ""));
        initialQuotes.add(new Quote("Action precedes motivation. Do the work and the inspiration will follow.", "David Viscott", false, ""));
        initialQuotes.add(new Quote("The secret of getting ahead is getting started.", "Mark Twain", false, ""));
        initialQuotes.add(new Quote("Focus is a muscle. The more you practice saying no to distractions, the stronger you become.", "Cal Newport", false, ""));
        initialQuotes.add(new Quote("Consistency is the DNA of mastery.", "Robin Sharma", false, ""));
        initialQuotes.add(new Quote("It is not that we have a short time to live, but that we waste a lot of it.", "Seneca", false, ""));
        initialQuotes.add(new Quote("The pain of discipline is far less than the pain of regret.", "Jim Rohn", false, ""));

        database.quoteDao().insertAll(initialQuotes);
    }
}
