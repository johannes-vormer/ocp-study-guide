package learn.ocp.web

import learn.ocp.content.ContentRepository
import learn.ocp.content.Topics
import learn.ocp.progress.ProgressRepository
import learn.ocp.progress.SessionRequest
import learn.ocp.progress.StudyService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalDateTime

private fun notFound(): Nothing = throw ResponseStatusException(HttpStatus.NOT_FOUND)

@ControllerAdvice
class GlobalModel(private val content: ContentRepository) {
    @ModelAttribute("contentErrors")
    fun contentErrors() = content.errors
}

@Controller
class DashboardController(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
    private val study: StudyService,
) {
    @GetMapping("/")
    fun dashboard(model: Model): String {
        val plan = study.planDays()
        model.addAttribute("overall", study.overall())
        model.addAttribute("topics", study.topicRows())
        model.addAttribute("examDate", study.examDate())
        model.addAttribute("daysLeft", study.daysLeft())
        model.addAttribute("nextDay", plan.firstOrNull { !it.done })
        model.addAttribute("planDone", plan.count { it.done })
        model.addAttribute("planTotal", plan.size)
        model.addAttribute("sessions", progress.recentSessions(8))
        val perDay = progress.attemptsPerDay(28)
        model.addAttribute("activity", (27 downTo 0).map { back ->
            val d = LocalDate.now().minusDays(back.toLong())
            val (n, c) = perDay[d.toString()] ?: (0 to 0)
            Triple(d, n, c)
        })
        return "dashboard"
    }

    @PostMapping("/settings")
    fun settings(@RequestParam examDate: String): String {
        if (examDate.isNotBlank()) progress.saveSetting("exam-date", LocalDate.parse(examDate).toString())
        return "redirect:/"
    }

    @PostMapping("/reload")
    fun reload(): String {
        content.reload()
        return "redirect:/"
    }
}

@Controller
class StudyController(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
    private val study: StudyService,
) {
    @GetMapping("/study")
    fun index(model: Model): String {
        model.addAttribute("topics", study.topicRows())
        model.addAttribute("topicDocs", content.topicDocs().associateBy { it.topic })
        model.addAttribute("otherDocs", content.docs.values.filter { it.topic == null || it.topic !in Topics.all }.sortedBy { it.order })
        return "study"
    }

    @GetMapping("/study/{id}")
    fun doc(@PathVariable id: String, model: Model): String {
        val doc = content.docs[id] ?: notFound()
        model.addAttribute("doc", doc)
        val topic = doc.topic?.takeIf { it in Topics.all }
        model.addAttribute("topic", topic)
        if (topic != null) {
            model.addAttribute("progress", progress.topicProgress()[topic])
            model.addAttribute("stats", study.stats(content.questionsForTopic(topic)))
        }
        return "doc"
    }

    @PostMapping("/study/{topic}/progress")
    fun saveProgress(
        @PathVariable topic: String,
        @RequestParam status: String,
        @RequestParam(defaultValue = "0") confidence: Int,
        @RequestParam(defaultValue = "") notes: String,
        @RequestParam doc: String,
    ): String {
        if (topic !in Topics.all) notFound()
        progress.saveTopicProgress(topic, status, confidence, notes)
        return "redirect:/study/$doc"
    }

    @GetMapping("/plan")
    fun plan(model: Model): String {
        model.addAttribute("days", study.planDays())
        model.addAttribute("examDate", study.examDate())
        model.addAttribute("daysLeft", study.daysLeft())
        return "plan"
    }

    @PostMapping("/plan/{day}")
    fun toggleDay(@PathVariable day: Int, @RequestParam done: Boolean): String {
        progress.setPlanDay(day, done)
        return "redirect:/plan#day-$day"
    }
}

@Controller
class PracticeController(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
    private val study: StudyService,
) {
    @GetMapping("/practice")
    fun index(model: Model): String {
        val latest = progress.latestResults()
        val sets = content.sets.values.groupBy { it.source }
        model.addAttribute("sources", sets.mapValues { (_, list) ->
            list.map { it to study.stats(it.questionIds.mapNotNull { id -> content.questions[id] }, latest) }
        })
        model.addAttribute("topics", study.topicRows())
        return "practice"
    }

    @PostMapping("/practice/start")
    fun start(
        @RequestParam scope: String,
        @RequestParam(defaultValue = "all") filter: String,
        @RequestParam(defaultValue = "0") count: Int,
        @RequestParam(defaultValue = "false") shuffle: Boolean,
        @RequestParam(defaultValue = "practice") mode: String,
        model: Model,
    ): String {
        val id = study.startSession(SessionRequest(scope, filter, count, shuffle, mode))
            ?: return "redirect:/practice?empty"
        return "redirect:/session/$id/q/0"
    }

    @GetMapping("/session/{id}/q/{n}")
    fun question(@PathVariable id: Long, @PathVariable n: Int, model: Model): String {
        val session = progress.session(id) ?: notFound()
        if (session.finishedAt != null && session.isExam) return "redirect:/session/$id/result"
        if (session.isExam && session.deadline!!.isBefore(LocalDateTime.now())) {
            progress.finishSession(id); return "redirect:/session/$id/result"
        }
        val qid = session.questionIds.getOrNull(n) ?: return "redirect:/session/$id/result"
        val question = content.questions[qid]
        val attempts = progress.attempts(id)
        model.addAttribute("sess", session)
        model.addAttribute("n", n)
        model.addAttribute("q", question)
        model.addAttribute("qid", qid)
        model.addAttribute("attempt", attempts[qid])
        model.addAttribute("attempts", attempts)
        model.addAttribute("history", progress.latestResults()[qid])
        model.addAttribute("secondsLeft", session.deadline?.let { java.time.Duration.between(LocalDateTime.now(), it).seconds })
        model.addAttribute("topicTitle", question?.let { Topics.title(it.topic) })
        model.addAttribute("setTitle", question?.let { content.sets[it.setId]?.title })
        return "question"
    }

    @PostMapping("/session/{id}/q/{n}")
    fun answer(
        @PathVariable id: Long,
        @PathVariable n: Int,
        @RequestParam(required = false) answer: List<String>?,
        @RequestParam(defaultValue = "next") go: String,
    ): String {
        val session = progress.session(id) ?: notFound()
        val qid = session.questionIds.getOrNull(n) ?: notFound()
        val q = content.questions[qid] ?: notFound()
        val expired = session.isExam && session.deadline!!.isBefore(LocalDateTime.now())
        if (session.finishedAt == null && !expired && !answer.isNullOrEmpty()) {
            val selected = answer.map { it.uppercase() }.toSet()
            progress.saveAttempt(id, qid, selected, selected == q.answers)
        }
        if (expired) { progress.finishSession(id); return "redirect:/session/$id/result" }
        return when {
            !session.isExam -> "redirect:/session/$id/q/$n"        // show feedback
            go == "prev" -> "redirect:/session/$id/q/${maxOf(0, n - 1)}"
            go == "finish" -> { progress.finishSession(id); "redirect:/session/$id/result" }
            n + 1 >= session.questionIds.size -> "redirect:/session/$id/q/$n"
            else -> "redirect:/session/$id/q/${n + 1}"
        }
    }

    @PostMapping("/session/{id}/finish")
    fun finish(@PathVariable id: Long): String {
        progress.finishSession(id)
        return "redirect:/session/$id/result"
    }

    @GetMapping("/session/{id}/result")
    fun result(@PathVariable id: Long, model: Model): String {
        val session = progress.session(id) ?: notFound()
        val attempts = progress.attempts(id)
        val rows = session.questionIds.mapIndexed { i, qid -> Triple(i, content.questions[qid], attempts[qid]) }
        val correct = attempts.values.count { it.correct }
        model.addAttribute("sess", session)
        model.addAttribute("rows", rows)
        model.addAttribute("correct", correct)
        model.addAttribute("total", session.questionIds.size)
        model.addAttribute("pct", if (session.questionIds.isEmpty()) 0 else correct * 100 / session.questionIds.size)
        model.addAttribute("byTopic", rows.filter { it.second != null }.groupBy { it.second!!.topic }.mapValues { (_, r) ->
            r.count { it.third?.correct == true } to r.size
        }.mapKeys { Topics.title(it.key) })
        return "result"
    }
}
