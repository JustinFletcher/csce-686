

function run_scp_program(program_call, input_file_name)
   open(program_call, "w", STDOUT) do io

               println(io, input_file_name)
               println(io, "")
               println(io, "n")
               println(io, "")
               println(io, "n")
    end
end

function construct_random_set_matrix(n_sets, n_cover_elements, density)

    edges = density*((n_sets*n_cover_elements))

    mask = randperm((n_sets*n_cover_elements))[1:edges]
    a = reshape([(i in mask) ? 1 : 0 for i in 1:(n_sets*n_cover_elements)],(n_cover_elements,n_sets))

    return(a)

end

function set_matrix_to_input_file(a, filename, program_dir)
    f = open(program_dir*"\\"*filename, "w")

    num_elements = size(a)[1]
    num_sets = size(a)[2]

    write(f, "{")

    [write(f, " "*string(n)) for n in 1:num_elements]

    write(f, " }\n")
    write(f, "{\n")

    for set in 1:num_sets

        write(f, "\t(")
        write(f, "{")

        for covers_element in 1:num_elements
            if (a[covers_element,set]==1)
                write(f, " $covers_element")
            end
        end
        write(f, " }")
        cost=rand(1:5)
        write(f, ",$cost")
        write(f, ")\n")
    end

    write(f, "}")


    close(f)
end


#################

a = construct_random_set_matrix(100, 50, 0.3)

program_dir = "C:\\csce-686\\hw6\\scp_code\\original\\"
cd(program_dir)

set_matrix_to_input_file(a, "deleteme.txt", program_dir)

program_call = ` java -jar "SCP Solver 2006.jar" `
tic()
run_scp_program(program_call, "deleteme.txt")
runtime_unmod += toq()
######################
#################

program_dir = "C:\\csce-686\\hw6\\scp_code\\modified\\"
cd(program_dir)

set_matrix_to_input_file(a, "deleteme.txt", program_dir)

program_call = ` java -jar scpKF.jar deleteme.txt -H1 -H2 -H3 -noout`

tic()
run_scp_program(program_call, "deleteme.txt")
runtime += toq()
######################
runtime_unmod
runtime
println(runtime_unmod/runtime)
######################

function run_scp_program_experiment(program_call, program_dir, num_reps, n_sets_seq, n_elements_seq, density)

    cd(program_dir)

    runtime_matrix = zeros((num_reps,length(n_elements_seq),length(n_sets_seq)))
    for rep_num in 1:num_reps

        for n_elements in 1:length(n_elements_seq)
            for n_sets in 1:length(n_sets_seq)
                # Construct a random set cover problem
                a = construct_random_set_matrix(n_sets, n_elements, density)

                set_matrix_to_input_file(a, "temp_input_file.txt", program_dir)

                tic()
                run_scp_program(program_call, "temp_input_file.txt")
                runtime = toq()

                runtime_matrix[rep_num, n_elements, n_sets] = runtime

            end
        end

    end

    return(runtime_matrix)

end

##############################################
using PyPlot

function movingWindowAverage(inputVector, windowSize)

    movingAverageWindowVector = Float64[]

    for (windowMidIndex = 1:length(inputVector))

        windowStartIndex = maximum([minimum([windowMidIndex-floor(windowSize/2), (length(inputVector)-windowSize)]), 1])

        windowEndIndex = minimum([length(inputVector),(windowStartIndex+windowSize)])

        push!(movingAverageWindowVector, mean(inputVector[windowStartIndex:windowEndIndex]))

    end

  return(movingAverageWindowVector)

end



##############################################

# Select the experimental range.
num_reps = 30
n_sets_seq = [10:20:1500]
n_elements_seq = [10:5:100]


# Initialize the program calls.
original_scp_call = ` java -jar "SCP Solver 2006.jar" `
original_program_dir = "C:\\csce-686\\hw6\\scp_code\\original\\"


# Run the eperiment.
scp_runtime_mat = @time run_scp_program_experiment(original_scp_call, original_program_dir, num_reps, n_sets_seq, n_elements_seq, 0.30)


#############################

# Calculate the mean.
scp_runtime_mat_mean = slice(mean(scp_runtime_mat,1), 1,:,:)

# Plot the results.
figure(1)

subplot(2,3,(2,3))
imshow(scp_runtime_mat_mean,
       interpolation="none",
       extent=[n_sets_seq[1],n_sets_seq[end],n_elements_seq[1],n_elements_seq[end]],
       origin="lower",
       aspect=5)
colorbar(orientation="horizontal", label="  Running Time \$\ (s) \$\ ")
xlabel(" \$\ n \$\ (Number of Sets)")
ylabel(" \$\ n \$\ (Number of Elements to Cover)")
title("Mean Running Time of the SCP Program")
subplot(2,3,(5,6))
scp_runtime_by_nsets_mat_mean = mean(scp_runtime_mat_mean ,1)
scp_runtime_by_nsets_mat_std = std(scp_runtime_mat_mean ,1)

plot(n_sets_seq, vec(scp_runtime_by_nsets_mat_mean), label="Output Calculation Time", color="blue")
errorbar(n_sets_seq, vec(scp_runtime_by_nsets_mat_mean), yerr=vec(scp_runtime_by_nsets_mat_std), fmt=".", alpha=0.7, color="blue")
title("Set-Wise Marginal Mean Running Time of the SCP Program")
xlabel(" \$\ n \$\ (Number of Sets)")
ylabel("  Running Time \$\ (s) \$\ ")
legend(loc=2)
xlim(0,1500)

scp_runtime_by_nelemnts_mat_mean = mean(scp_runtime_mat_mean, 2)

subplot(2,3,1)
scp_runtime_by_nelements_mat_mean = mean(scp_runtime_mat_mean ,2)
scp_runtime_by_nelements_mat_std = std(scp_runtime_mat_mean ,2)

plot(n_elements_seq, vec(scp_runtime_by_nelements_mat_mean), label="Output Calculation Time", color="blue")
errorbar(n_elements_seq, vec(scp_runtime_by_nelements_mat_mean), yerr=vec(scp_runtime_by_nelements_mat_std), fmt=".", alpha=0.7, color="blue")
title("Cover-Element-Wise Marginal Mean Running Time of the SCP Program")
xlabel(" \$\ n \$\ (Number of Elements to Cover)")
ylabel("  Running Time \$\ (s) \$\ ")
legend(loc=2)




##############################################

# Select the experimental range.
num_reps = 30
n_sets_seq = [10:20:1500]
n_elements_seq = [10:5:100]


# Initialize the program calls.
modified_scp_call = ` java -jar scpKF.jar temp_input_file.txt -H1 -noout`
modified_program_dir = "C:\\csce-686\\hw6\\scp_code\\modified\\"

# Run the eperiment.
modified_scp_runtime_mat = @time run_scp_program_experiment(modified_scp_call, modified_program_dir, num_reps, n_sets_seq, n_elements_seq, 0.30)

#############################

# Calculate the mean.
modified_scp_runtime_mat_mean = slice(mean(modified_scp_runtime_mat,1), 1,:,:)

# Pplot the results.
figure(2)
subplot(2,3,(2,3))
imshow(modified_scp_runtime_mat_mean,
       interpolation="none",
       extent=[n_sets_seq[1],n_sets_seq[end],n_elements_seq[1],n_elements_seq[end]],
       origin="lower",
       aspect=5)
colorbar(orientation="horizontal", label="  Running Time \$\ (s) \$\ ")
xlabel(" \$\ n \$\ (Number of Sets)")
ylabel(" \$\ n \$\ (Number of Elements to Cover)")
title("Mean Running Time of the SCP Program")


subplot(2,3,(5,6))
modified_scp_runtime_by_nsets_mat_mean = mean(modified_scp_runtime_mat_mean ,1)
modified_scp_runtime_by_nsets_mat_std = std(modified_scp_runtime_mat_mean ,1)

plot(n_sets_seq, vec(modified_scp_runtime_by_nsets_mat_mean), label="Output Calculation Time", color="blue")
errorbar(n_sets_seq, vec(modified_scp_runtime_by_nsets_mat_mean), yerr=vec(modified_scp_runtime_by_nsets_mat_std), fmt=".", alpha=0.7, color="blue")
title("Set-Wise Marginal Mean Running Time of the Modified SCP Program")
xlabel(" \$\ n \$\ (Number of Sets)")
ylabel(" \$\ t \$\ \$\ (s) \$\ ")
legend(loc=2)
xlim(0,1500)

modified_scp_runtime_by_nelemnts_mat_mean = mean(modified_scp_runtime_mat_mean, 2)


subplot(2,3,1)
modified_scp_runtime_by_nelements_mat_mean = mean(modified_scp_runtime_mat_mean ,2)
modified_scp_runtime_by_nelements_mat_std = std(modified_scp_runtime_mat_mean ,2)

plot(n_elements_seq, vec(modified_scp_runtime_by_nelements_mat_mean), label="Output Calculation Time", color="blue")
errorbar(n_elements_seq, vec(modified_scp_runtime_by_nelements_mat_mean), yerr=vec(modified_scp_runtime_by_nelements_mat_std), fmt=".", alpha=0.7, color="blue")
title("Cover-Element-Wise Marginal Mean Running Time of the Modified SCP Program")
xlabel(" \$\ n \$\ (Number of Elemtns to Cover)")
ylabel(" \$\ t \$\ \$\ (s) \$\ ")
legend(loc=2)
